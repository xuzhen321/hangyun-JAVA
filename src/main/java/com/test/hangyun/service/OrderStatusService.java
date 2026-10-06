package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.OrderStatusQueryReq;
import com.test.hangyun.dto.OrderStatusReq;
import com.test.hangyun.dto.vo.OrderStatusVO;

import java.util.List;

public interface OrderStatusService {

    /** 分页查询 */
    PageResult<OrderStatusVO> page(OrderStatusQueryReq req);

    /** 详情 */
    OrderStatusVO getById(Long id);

    /** 新增 */
    void create(OrderStatusReq req);

    /** 修改 */
    void update(Long id, OrderStatusReq req);

    /** 删除, 内置状态或有订单引用时拒绝 */
    void delete(Long id);

    /** 全部状态, 供前端下拉框使用 */
    List<OrderStatusVO> listAll();
}
