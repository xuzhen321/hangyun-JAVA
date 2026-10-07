package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物种类表 cargo_type 。
 * <p>
 * 报告中的关系模式: Cargo_Type(id, name, description, weight_kg, volume_m3)
 * weight_kg / volume_m3 是**单件**的重量和体积, 不是总量。
 */
@Data
@TableName("cargo_type")
public class CargoType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 货物名称 */
    private String name;

    /** 货物描述 */
    private String description;

    /** 单件重量(kg), decimal(10,2) */
    private BigDecimal weightKg;

    /** 单件体积(m³), decimal(10,2) */
    private BigDecimal volumeM3;

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
