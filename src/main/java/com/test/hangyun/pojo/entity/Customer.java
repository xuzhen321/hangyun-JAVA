package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户表 customer （写用）。
 * 报告中的关系模式: Customer(id, name, phone, email, address,
 *                          qualification, qualification_valid_to, status_id)
 */
@Data
@TableName("customer")
public class Customer {

    /** 主键, 自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String address;
    private String qualification;

    /** 资质有效期至 */
    private LocalDateTime qualificationValidTo;

    /** 客户状态ID, 逻辑外键 -> customer_status.id */
    private Long statusId;

    /**
     * 创建时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime insertTime;

    /**
     * 修改时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     * 触发器只在整行数据确实发生变化时才会刷新它。
     */
    private LocalDateTime updateTime;
}
