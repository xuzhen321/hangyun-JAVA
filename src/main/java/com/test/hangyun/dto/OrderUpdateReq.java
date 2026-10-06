package com.test.hangyun.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改订单请求。语义是整体覆盖: 未传的字段会被置为 null。
 * <p>
 * 注意: **不含 id** —— 订单号不可修改, 它在路径参数里。
 * 也不含 insert_time / update_time, 由数据库触发器维护。
 */
@Data
public class OrderUpdateReq {

    /** 客户ID, 必须是 customer 中已存在的记录 */
    @NotNull(message = "客户不能为空")
    private Long customerId;

    /** 下单时间 */
    private LocalDateTime orderDate;

    /** 起运港ID, 必须是 port 中已存在的记录 */
    private Long loadingPortId;

    /** 目的港ID, 必须是 port 中已存在的记录 */
    private Long dischargePortId;

    /** 订单状态ID, 必须是 order_status 中已存在的记录 */
    private Long statusId;
}
