package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.view.OrderView;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单响应对象。
 * 与视图对象分开, 保证接口契约不随视图列变化而波动。
 */
@Data
public class OrderVO {

    /** 订单号 */
    private String id;

    /** 客户ID */
    private Long customerId;

    /** 客户姓名（来自 customer.name, 后端已联表带出） */
    private String customerName;

    /** 下单时间 */
    private LocalDateTime orderDate;

    /** 起运港ID */
    private Long loadingPortId;

    /** 起运港中文名 */
    private String loadingPortName;

    /** 目的港ID */
    private Long dischargePortId;

    /** 目的港中文名 */
    private String dischargePortName;

    /** 订单状态ID */
    private Long status;

    /** 订单状态中文描述 */
    private String statusDescription;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static OrderVO from(OrderView v) {
        if (v == null) {
            return null;
        }
        OrderVO vo = new OrderVO();
        vo.setId(v.getId());
        vo.setCustomerId(v.getCustomerId());
        vo.setCustomerName(v.getCustomerName());
        vo.setOrderDate(v.getOrderDate());
        vo.setLoadingPortId(v.getLoadingPortId());
        vo.setLoadingPortName(v.getLoadingPortName());
        vo.setDischargePortId(v.getDischargePortId());
        vo.setDischargePortName(v.getDischargePortName());
        vo.setStatus(v.getStatus());
        vo.setStatusDescription(v.getStatusDescription());
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        return vo;
    }
}
