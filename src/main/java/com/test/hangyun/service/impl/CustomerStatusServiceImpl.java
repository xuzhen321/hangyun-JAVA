package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.CustomerStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CustomerStatusQueryReq;
import com.test.hangyun.dto.CustomerStatusReq;
import com.test.hangyun.dto.vo.CustomerStatusVO;
import com.test.hangyun.mapper.CustomerStatusMapper;
import com.test.hangyun.pojo.entity.CustomerStatus;
import com.test.hangyun.service.CustomerStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户状态字典。
 * <p>
 * 状态描述在库里有 unique 约束, 且 customer.status_id 逻辑引用它,
 * 所以新增/修改前查重、删除前查引用, 都必须在应用层做。
 * insert_time / update_time 交给触发器, 应用层不赋值。
 */
@Service
@RequiredArgsConstructor
public class CustomerStatusServiceImpl implements CustomerStatusService {

    private final CustomerStatusMapper customerStatusMapper;

    @Override
    public PageResult<CustomerStatusVO> page(CustomerStatusQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        // 字典表只做分页, 不带任何筛选条件
        LambdaQueryWrapper<CustomerStatus> w = new LambdaQueryWrapper<>();
        w.orderByAsc(CustomerStatus::getId);

        Page<CustomerStatus> p = customerStatusMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, CustomerStatusVO::from);
    }

    @Override
    public CustomerStatusVO getById(Long id) {
        return CustomerStatusVO.from(getExisting(id));
    }

    @Override
    @Transactional
    public void create(CustomerStatusReq req) {
        String description = req.getDescription().trim();
        ensureDescriptionUnique(description, null);

        CustomerStatus e = new CustomerStatus();
        e.setDescription(description);
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        customerStatusMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, CustomerStatusReq req) {
        // 内置状态(1正常/2异常/3注销)是系统基础数据, 一律不允许修改。
        // 这一条与库里存不存在无关, 所以放在存在性校验之前先挡掉。
        if (CustomerStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("系统内置状态(正常/异常/注销)不允许修改");
        }

        getExisting(id);
        String description = req.getDescription().trim();
        // 查重时排除自己, 否则"只改其他字段、描述不变"的提交会被误判为重复
        ensureDescriptionUnique(description, id);

        LambdaUpdateWrapper<CustomerStatus> u = new LambdaUpdateWrapper<>();
        u.eq(CustomerStatus::getId, id)
                .set(CustomerStatus::getDescription, description);
        customerStatusMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 内置状态(1正常/2异常/3注销)是系统基础数据, 一律不允许删除。
        // 这一条与库里存不存在无关, 所以放在存在性校验之前先挡掉。
        if (CustomerStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("系统内置状态(正常/异常/注销)不允许删除");
        }

        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被客户引用的状态
        long customers = customerStatusMapper.countCustomersByStatusId(id);
        if (customers > 0) {
            throw BizException.conflict("该状态下存在 " + customers + " 个客户, 无法删除");
        }
        customerStatusMapper.deleteById(id);
    }

    @Override
    public List<CustomerStatusVO> listAll() {
        return customerStatusMapper.selectList(
                        new LambdaQueryWrapper<CustomerStatus>().orderByAsc(CustomerStatus::getId))
                .stream().map(CustomerStatusVO::from).toList();
    }

    private CustomerStatus getExisting(Long id) {
        CustomerStatus e = customerStatusMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("客户状态不存在");
        }
        return e;
    }

    /** 描述在库里是 unique, 提前查重以便返回 409 而不是撞唯一键报 500 */
    private void ensureDescriptionUnique(String description, Long excludeId) {
        LambdaQueryWrapper<CustomerStatus> w = new LambdaQueryWrapper<>();
        w.eq(CustomerStatus::getDescription, description);
        w.ne(excludeId != null, CustomerStatus::getId, excludeId);
        if (customerStatusMapper.selectCount(w) > 0) {
            throw BizException.conflict("状态描述已存在: " + description);
        }
    }
}
