package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
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
        if (StringUtils.hasText(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            w.and(q -> q.like(CustomerView::getName, kw).or().like(CustomerView::getPhone, kw));
        }
        w.eq(req.getStatus() != null, CustomerView::getStatus, req.getStatus());
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
    public void delete(Long id) {
        if (customerViewMapper.selectById(id) == null) {
            throw BizException.notFound("客户不存在");
        }
        // 删除保护: 库里没有外键约束, 必须自己挡住被引用的记录
        long orders = customerMapper.countOrdersByCustomerId(id);
        if (orders > 0) {
            throw BizException.conflict("该客户下存在 " + orders + " 个订单, 无法删除");
        }
        customerMapper.deleteById(id);
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
