package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单信息表 orders （写用）。
 * 报告中的关系模式: Orders(id, customer_id, order_date,
 *                          loading_port_id, discharge_port_id, status_id)
 * <p>
 * 注意表名: 报告里叫 Order, 是 SQL 保留字, 建表时改成了 Orders (initial.sql 有说明)。
 */
@Data
@TableName("orders")
public class Order {

    /**
     * 订单号, varchar(50)。
     * 用雪花算法生成(IdType.ASSIGN_ID): 插入时 MyBatis-Plus 自动塞一个 19 位数字串,
     * 应用层不用管, 也不用查重。代价是不可读、无业务含义。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /** 客户ID, 逻辑外键 -> customer.id */
    private Long customerId;

    /** 下单时间 */
    private LocalDateTime orderDate;

    /** 起运港ID, 逻辑外键 -> port.id */
    private Long loadingPortId;

    /** 目的港ID, 逻辑外键 -> port.id */
    private Long dischargePortId;

    /** 订单状态ID, 逻辑外键 -> order_status.id */
    private Long statusId;

    /**
     * 创建时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime insertTime;

    /**
     * 修改时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime updateTime;
}
