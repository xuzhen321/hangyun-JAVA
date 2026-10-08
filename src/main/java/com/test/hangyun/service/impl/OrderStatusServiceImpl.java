package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.OrderStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.OrderStatusQueryReq;
import com.test.hangyun.dto.OrderStatusReq;
import com.test.hangyun.dto.vo.OrderStatusVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.OrderStatusMapper;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.entity.OrderStatus;
import com.test.hangyun.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单状态字典。
 * <p>
 * 结构和客户状态完全对称: 描述在库里有 unique 约束, 且 orders.status_id 逻辑引用它,
 * 所以新增/修改前查重、删除前查引用, 都必须在应用层做。
 * insert_time / update_time 交给触发器, 应用层不赋值。
 */
@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusMapper orderStatusMapper;

    @Override
    public PageResult<OrderStatusVO> page(OrderStatusQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        // 字典表只做分页, 不带任何筛选条件
        LambdaQueryWrapper<OrderStatus> w = new LambdaQueryWrapper<>();
        w.orderByAsc(OrderStatus::getId);

        Page<OrderStatus> p = orderStatusMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, OrderStatusVO::from);
    }

    @Override
    public OrderStatusVO getById(Long id) {
        return OrderStatusVO.from(getExisting(id));
    }

    @Override
    @Transactional
    @OpLog(module = "订单状态", table = "order_status", type = OpType.INSERT, desc = "新增订单状态")
    public void create(OrderStatusReq req) {
        String description = req.getDescription().trim();
        ensureDescriptionUnique(description, null);

        OrderStatus e = new OrderStatus();
        e.setDescription(description);
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        orderStatusMapper.insert(e);
    }

    @Override
    @Transactional
    @OpLog(module = "订单状态", table = "order_status", type = OpType.UPDATE, desc = "修改订单状态")
    public void update(Long id, OrderStatusReq req) {
        // 内置状态(1已确认/2执行中/3已完成/4已取消)是系统基础数据, 一律不允许修改。
        // 这一条与库里存不存在无关, 所以放在存在性校验之前先挡掉。
        if (OrderStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("系统内置状态(已确认/执行中/已完成/已取消)不允许修改");
        }
        getExisting(id);
        String description = req.getDescription().trim();
        // 查重时排除自己, 否则"只改其他字段、描述不变"的提交会被误判为重复
        ensureDescriptionUnique(description, id);

        LambdaUpdateWrapper<OrderStatus> u = new LambdaUpdateWrapper<>();
        u.eq(OrderStatus::getId, id)
                .set(OrderStatus::getDescription, description);
        orderStatusMapper.update(null, u);
    }

    @Override
    @Transactional
    @OpLog(module = "订单状态", table = "order_status", type = OpType.DELETE, desc = "删除订单状态")
    public void delete(Long id) {
        // 内置状态(1已确认/2执行中/3已完成/4已取消)是系统基础数据, 一律不允许删除。
        // 这一条与库里存不存在无关, 所以放在存在性校验之前先挡掉。
        if (OrderStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("系统内置状态(已确认/执行中/已完成/已取消)不允许删除");
        }

        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被订单引用的状态
        long orders = orderStatusMapper.countOrdersByStatusId(id);
        if (orders > 0) {
            throw BizException.conflict("该状态下存在 " + orders + " 个订单, 无法删除");
        }
        orderStatusMapper.deleteById(id);
    }

    @Override
    public List<OrderStatusVO> listAll() {
        return orderStatusMapper.selectList(
                        new LambdaQueryWrapper<OrderStatus>().orderByAsc(OrderStatus::getId))
                .stream().map(OrderStatusVO::from).toList();
    }

    private OrderStatus getExisting(Long id) {
        OrderStatus e = orderStatusMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("订单状态不存在");
        }
        return e;
    }

    /** 描述在库里是 unique, 提前查重以便返回 409 而不是撞唯一键报 500 */
    private void ensureDescriptionUnique(String description, Long excludeId) {
        LambdaQueryWrapper<OrderStatus> w = new LambdaQueryWrapper<>();
        w.eq(OrderStatus::getDescription, description);
        w.ne(excludeId != null, OrderStatus::getId, excludeId);
        if (orderStatusMapper.selectCount(w) > 0) {
            throw BizException.conflict("状态描述已存在: " + description);
        }
    }
}
