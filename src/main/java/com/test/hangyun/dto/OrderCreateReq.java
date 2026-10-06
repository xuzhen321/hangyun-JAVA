package com.test.hangyun.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新增订单请求。
 * <p>
 * 注意: **不含 id** —— 订单号由后端用雪花算法生成, 前端不传。
 * 也不含 insert_time / update_time, 那两个由触发器维护。
 */
@Data
public class OrderCreateReq {

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
