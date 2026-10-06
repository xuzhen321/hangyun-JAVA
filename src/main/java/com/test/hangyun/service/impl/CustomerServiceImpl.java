package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.CustomerStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CustomerCreateReq;
import com.test.hangyun.dto.CustomerQueryReq;
import com.test.hangyun.dto.CustomerUpdateReq;
import com.test.hangyun.dto.vo.CustomerVO;
import com.test.hangyun.mapper.CustomerMapper;
import com.test.hangyun.mapper.CustomerStatusMapper;
import com.test.hangyun.mapper.CustomerViewMapper;
import com.test.hangyun.pojo.entity.Customer;
import com.test.hangyun.pojo.view.CustomerView;
import com.test.hangyun.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户管理。
 * <p>
 * 两条主线:
 *  - 查询走视图 v_customer, 一次拿全状态描述
 *  - 写走表 customer, insert_time / update_time 交给触发器, 应用层不赋值
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerViewMapper customerViewMapper;
    private final CustomerStatusMapper customerStatusMapper;

    @Override
    public PageResult<CustomerVO> page(CustomerQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<CustomerView> w = new LambdaQueryWrapper<>();

        // 前缀匹配, 不用 like('%%'): Customer 上的 name / qualification 是 btree 索引,
        // 只有"从头匹配"(name LIKE 'x%')才可能走索引, 两边都带 % 只能全表扫描。
        if (StringUtils.hasText(req.getName())) {
            w.likeRight(CustomerView::getName, req.getName().trim());
        }
        if (StringUtils.hasText(req.getQualification())) {
            w.likeRight(CustomerView::getQualification, req.getQualification().trim());
        }

        // 资质有效期区间, 两端都是闭区间(>= from, <= to), 单边不传就只限一边
        w.ge(req.getQualificationValidToFrom() != null,
                CustomerView::getQualificationValidTo, req.getQualificationValidToFrom());
        w.le(req.getQualificationValidToTo() != null,
                CustomerView::getQualificationValidTo, req.getQualificationValidToTo());

        // 逻辑删除: 已注销的客户默认不出现在列表里, 只有前端显式按 status=3 筛选时才列出来。
        // status_id 允许为 null, 而 "status <> 3" 对 null 求值为 null(视为不成立),
        // 会连"没有状态"的客户一起漏掉, 所以必须额外放行 null。
        if (req.getStatus() != null) {
            w.eq(CustomerView::getStatus, req.getStatus());
        } else {
            w.and(q -> q.ne(CustomerView::getStatus, CustomerStatusConstants.STATUS_CANCELLED)
                    .or().isNull(CustomerView::getStatus));
        }
        // 最新录入的排在最前面
        w.orderByDesc(CustomerView::getId);

        Page<CustomerView> p = customerViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, CustomerVO::from);
    }

    @Override
    public CustomerVO getById(Long id) {
        CustomerView v = customerViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("客户不存在");
        }
        return CustomerVO.from(v);
    }

    @Override
    @Transactional
    public void create(CustomerCreateReq req) {
        // 库里没有物理外键, 关联状态是否存在必须由应用层校验
        validateStatusExists(req.getStatusId());

        Customer e = new Customer();
        e.setName(req.getName());
        e.setPhone(req.getPhone());
        e.setEmail(req.getEmail());
        e.setAddress(req.getAddress());
        e.setQualification(req.getQualification());
        e.setQualificationValidTo(req.getQualificationValidTo());
        e.setStatusId(req.getStatusId());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间

        customerMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, CustomerUpdateReq req) {
        if (customerViewMapper.selectById(id) == null) {
            throw BizException.notFound("客户不存在");
        }
        validateStatusExists(req.getStatusId());

        // 用 UpdateWrapper 显式列出要改的列:
        //  1. 后端传的 null 能真正写进去(把字段清空), 不会被 "非空才更新" 策略跳过
        //  2. 结构上保证不会碰到 insert_time / update_time, 那两个由触发器管
        LambdaUpdateWrapper<Customer> u = new LambdaUpdateWrapper<>();
        u.eq(Customer::getId, id)
                .set(Customer::getName, req.getName())
                .set(Customer::getPhone, req.getPhone())
                .set(Customer::getEmail, req.getEmail())
                .set(Customer::getAddress, req.getAddress())
                .set(Customer::getQualification, req.getQualification())
                .set(Customer::getQualificationValidTo, req.getQualificationValidTo())
                .set(Customer::getStatusId, req.getStatusId());
        customerMapper.update(null, u);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 去重: 前端多选时可能因为交互传进重复的 id, 去重后 IN 里少几个参数
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }

        // 逻辑删除, 一条 UPDATE 覆盖整批。
        // 语义是宽松的: 库里不存在的 id 不会被匹配到, 自然被忽略 —— 这正是幂等的来源,
        // 重复提交同一批 id 或混进已注销的客户都不会报错。
        // 整行没有实际变化的记录(本来就是注销态), 触发器也不会刷新 update_time。
        LambdaUpdateWrapper<Customer> u = new LambdaUpdateWrapper<>();
        u.in(Customer::getId, distinctIds)
                .set(Customer::getStatusId, CustomerStatusConstants.STATUS_CANCELLED);
        customerMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (customerViewMapper.selectById(id) == null) {
            throw BizException.notFound("客户不存在");
        }

        // 逻辑删除: 不删行, 只把状态改成"注销"。
        // 好处是订单等引用不会悬空, 所以这里不需要再做引用检查。
        // 已经是注销状态时重复调用也安全: 整行没有实际变化, 触发器不会刷新 update_time。
        LambdaUpdateWrapper<Customer> u = new LambdaUpdateWrapper<>();
        u.eq(Customer::getId, id)
                .set(Customer::getStatusId, CustomerStatusConstants.STATUS_CANCELLED);
        customerMapper.update(null, u);
    }

    private void validateStatusExists(Long statusId) {
        if (statusId == null) {
            return;
        }
        if (customerStatusMapper.selectById(statusId) == null) {
            throw new BizException("客户状态不存在: " + statusId);
        }
    }
}
