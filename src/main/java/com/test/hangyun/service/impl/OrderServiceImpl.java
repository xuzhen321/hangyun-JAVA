package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.OrderStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CustomerOrderQueryReq;
import com.test.hangyun.dto.OrderCreateReq;
import com.test.hangyun.dto.OrderQueryReq;
import com.test.hangyun.dto.OrderUpdateReq;
import com.test.hangyun.dto.vo.OrderVO;
import com.test.hangyun.mapper.CustomerMapper;
import com.test.hangyun.mapper.OrderMapper;
import com.test.hangyun.mapper.OrderStatusMapper;
import com.test.hangyun.mapper.OrderViewMapper;
import com.test.hangyun.mapper.PortMapper;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.Order;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.view.OrderView;
import com.test.hangyun.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 订单管理。
 * <p>
 * 两条主线:
 *  - 查询走视图 v_order, 一次拿全客户姓名、起运港/目的港名称、状态描述
 *  - 写走表 orders, 订单号由雪花算法生成, insert_time / update_time 交给触发器
 * <p>
 * 库里没有物理外键, 所有 xxx_id 的存在性都由应用层校验。
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderViewMapper orderViewMapper;
    private final CustomerMapper customerMapper;
    private final OrderStatusMapper orderStatusMapper;
    private final PortMapper portMapper;

    @Override
    public PageResult<OrderVO> page(OrderQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<OrderView> w = buildWrapper(req);

        // 订单号是雪花算法的 19 位数字串, 定长, 所以按字符串倒序等价于按时间倒序(最新在前)。
        // 若将来订单号长度会变, 这里的字典序就不再等于数值序, 需要改成按 insert_time 排序。
        w.orderByDesc(OrderView::getId);

        Page<OrderView> p = orderViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, OrderVO::from);
    }

    @Override
    @OpLog(module = "订单管理", table = "orders", type = OpType.EXPORT, desc = "导出 Excel")
    public List<OrderVO> listForExport(OrderQueryReq req) {
        LambdaQueryWrapper<OrderView> w = buildWrapper(req);
        w.orderByDesc(OrderView::getId);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<OrderView> rows = orderViewMapper.selectList(
                w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return rows.stream().map(OrderVO::from).toList();
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<OrderView> buildWrapper(OrderQueryReq req) {
        LambdaQueryWrapper<OrderView> w = new LambdaQueryWrapper<>();

        // 按客户姓名前缀匹配(不是 id): 列表筛选是给人用的, 没人记得住客户 id。
        // 用 likeRight 而不是 like —— 视图里的 customer_name 来自 customer.name,
        // 条件会下推到基表, 才能用上那个 varchar_pattern_ops 索引。
        if (StringUtils.hasText(req.getCustomerName())) {
            w.likeRight(OrderView::getCustomerName, req.getCustomerName().trim());
        }

        // 逻辑删除: 已取消的订单默认不出现在列表里, 只有前端显式按 status=4 筛选时才列出来。
        // status_id 允许为 null, 而 "status <> 4" 对 null 求值为 null(视为不成立),
        // 会连"没有状态"的订单一起漏掉, 所以必须额外放行 null。
        if (req.getStatus() != null) {
            w.eq(OrderView::getStatus, req.getStatus());
        } else {
            w.and(q -> q.ne(OrderView::getStatus, OrderStatusConstants.STATUS_CANCELLED)
                    .or().isNull(OrderView::getStatus));
        }

        // 下单时间区间, 两端都是闭区间(>= from, <= to), 单边不传就只限一边
        w.ge(req.getOrderDateFrom() != null, OrderView::getOrderDate, req.getOrderDateFrom());
        w.le(req.getOrderDateTo() != null, OrderView::getOrderDate, req.getOrderDateTo());
        return w;
    }

    @Override
    public PageResult<OrderVO> pageByCustomer(Long customerId, CustomerOrderQueryReq req) {
        // 客户不存在时给 404, 而不是返回一个空页 ——
        // 否则前端分不清"这个客户确实没订单"和"客户 id 传错了"。
        if (customerMapper.selectById(customerId) == null) {
            throw BizException.notFound("客户不存在");
        }

        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<OrderView> w = new LambdaQueryWrapper<>();
        w.eq(OrderView::getCustomerId, customerId);
        // 刻意不过滤状态: 这里要的是"该客户的全部订单", 已取消的也算他的往来记录。
        // 注意这与 /orders 列表的口径不同(那边默认排除已取消), 是有意为之。
        w.orderByDesc(OrderView::getId);

        Page<OrderView> p = orderViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, OrderVO::from);
    }

    @Override
    public OrderVO getById(String id) {
        OrderView v = orderViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("订单不存在");
        }
        return OrderVO.from(v);
    }

    @Override
    @OpLog(module = "订单管理", table = "orders", type = OpType.INSERT, desc = "新增订单")
    @Transactional
    public void create(OrderCreateReq req) {
        // 库里没有物理外键, 关联记录是否存在必须由应用层校验
        validateCustomerExists(req.getCustomerId());
        validatePortExists(req.getLoadingPortId(), "起运港");
        validatePortExists(req.getDischargePortId(), "目的港");
        validateStatusExists(req.getStatusId());

        Order e = new Order();
        // id 刻意不赋值: MyBatis-Plus 用雪花算法生成后再写库
        e.setCustomerId(req.getCustomerId());
        e.setOrderDate(req.getOrderDate());
        e.setLoadingPortId(req.getLoadingPortId());
        e.setDischargePortId(req.getDischargePortId());
        e.setStatusId(req.getStatusId());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间

        orderMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(String id, OrderUpdateReq req) {
        if (orderViewMapper.selectById(id) == null) {
            throw BizException.notFound("订单不存在");
        }
        validateCustomerExists(req.getCustomerId());
        validatePortExists(req.getLoadingPortId(), "起运港");
        validatePortExists(req.getDischargePortId(), "目的港");
        validateStatusExists(req.getStatusId());

        // 用 UpdateWrapper 显式列出要改的列:
        //  1. 后端传的 null 能真正写进去(把字段清空), 不会被 "非空才更新" 策略跳过
        //  2. 结构上保证不会碰到主键和 insert_time / update_time
        LambdaUpdateWrapper<Order> u = new LambdaUpdateWrapper<>();
        u.eq(Order::getId, id)
                .set(Order::getCustomerId, req.getCustomerId())
                .set(Order::getOrderDate, req.getOrderDate())
                .set(Order::getLoadingPortId, req.getLoadingPortId())
                .set(Order::getDischargePortId, req.getDischargePortId())
                .set(Order::getStatusId, req.getStatusId());
        orderMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (orderViewMapper.selectById(id) == null) {
            throw BizException.notFound("订单不存在");
        }

        // 逻辑删除: 不删行, 只把状态改成"已取消"。
        // 好处是货物(Cargo)等引用不会悬空, 所以这里不需要再做引用检查。
        // 已经是已取消时重复调用也安全: 整行没有实际变化, 触发器不会刷新 update_time。
        LambdaUpdateWrapper<Order> u = new LambdaUpdateWrapper<>();
        u.eq(Order::getId, id)
                .set(Order::getStatusId, OrderStatusConstants.STATUS_CANCELLED);
        orderMapper.update(null, u);
    }

    @Override
    @Transactional
    public void deleteBatch(List<String> ids) {
        // 去重: 前端多选时可能传进重复的订单号
        List<String> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }

        // 逻辑删除, 一条 UPDATE 覆盖整批。
        // 语义是宽松的: 库里不存在的订单号匹配不到, 自然被忽略 —— 这正是幂等的来源,
        // 重复提交同一批订单号或混进已取消的订单都不会报错。
        LambdaUpdateWrapper<Order> u = new LambdaUpdateWrapper<>();
        u.in(Order::getId, distinctIds)
                .set(Order::getStatusId, OrderStatusConstants.STATUS_CANCELLED);
        orderMapper.update(null, u);
    }

    private void validateCustomerExists(Long customerId) {
        if (customerId == null) {
            return;
        }
        if (customerMapper.selectById(customerId) == null) {
            // 文案里不带 id, 那是内部实现细节; 具体值走 detail 只进日志
            throw new BizException("客户不存在", "customerId=" + customerId);
        }
    }

    private void validateStatusExists(Long statusId) {
        if (statusId == null) {
            return;
        }
        if (orderStatusMapper.selectById(statusId) == null) {
            throw new BizException("订单状态不存在", "statusId=" + statusId);
        }
    }

    /** portName 用来区分是起运港还是目的港没查到 */
    private void validatePortExists(Long portId, String portName) {
        if (portId == null) {
            return;
        }
        if (portMapper.selectById(portId) == null) {
            throw new BizException(portName + "不存在", portName + "Id=" + portId);
        }
    }
}
