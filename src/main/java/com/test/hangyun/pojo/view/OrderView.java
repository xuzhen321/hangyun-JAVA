package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单管理视图 v_order （只读）。
 * 已经联好了客户名称、起运港/目的港名称、订单状态描述, 查询一次即可拿到, 无需前端二次联查。
 * <p>
 * 对应列: id, customer_id, customer_name, order_date,
 *         loading_port_id, loading_port_name, discharge_port_id, discharge_port_name,
 *         status, status_description, insert_time, update_time
 */
@Data
@TableName("v_order")
public class OrderView {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /** 客户ID, 逻辑外键 -> customer.id */
    private Long customerId;

    /** 来自 customer.name */
    private String customerName;

    /** 下单时间 */
    private LocalDateTime orderDate;

    /** 起运港ID, 逻辑外键 -> port.id */
    private Long loadingPortId;

    /** 来自 port.cnname */
    private String loadingPortName;

    /** 目的港ID, 逻辑外键 -> port.id */
    private Long dischargePortId;

    /** 来自 port.cnname */
    private String dischargePortName;

    /** 视图里是 orders.status_id, 别名 status */
    private Long status;

    /** 来自 order_status.description */
    private String statusDescription;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
