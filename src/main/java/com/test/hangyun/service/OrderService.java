package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CustomerOrderQueryReq;
import com.test.hangyun.dto.OrderCreateReq;
import com.test.hangyun.dto.OrderQueryReq;
import com.test.hangyun.dto.OrderUpdateReq;
import com.test.hangyun.dto.vo.OrderVO;

import java.util.List;

public interface OrderService {

    /** 分页查询(读视图 v_order) */
    PageResult<OrderVO> page(OrderQueryReq req);

    /**
     * 某个客户名下的**全部**订单(分页, 读视图 v_order)。
     * <p>
     * 不做状态过滤 —— 已取消的也算这个客户的往来记录。客户不存在时抛 404。
     */
    PageResult<OrderVO> pageByCustomer(Long customerId, CustomerOrderQueryReq req);

    /** 详情(读视图 v_order) */
    OrderVO getById(String id);

    /** 新增(写表 orders), 订单号由后端生成, 不返回数据 */
    void create(OrderCreateReq req);

    /** 修改(写表 orders), 不返回数据 */
    void update(String id, OrderUpdateReq req);

    /** 逻辑删除: 把状态改成"已取消" */
    void delete(String id);

    /** 批量逻辑删除: 不存在的订单号忽略 */
    void deleteBatch(List<String> ids);
}
