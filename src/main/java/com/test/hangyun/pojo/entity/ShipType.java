package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶类型字典表 ship_type 。
 * <p>
 * ⚠️ **注意 GT / NT / DWT 三列**：建表脚本里它们是**带双引号的大写列名**
 * （`"GT" decimal(12,2)`），PostgreSQL 会保持大小写。所以这里必须用
 * {@code @TableField("\"GT\"")} 显式写出带引号的列名 —— 否则 MP 生成的是不带引号的
 * {@code GT}，PG 会折叠成小写 {@code gt}，报"列不存在"。
 * <p>
 * 被 vessel.vessel_type_id 引用, 删除前要查引用。
 */
@Data
@TableName("ship_type")
public class ShipType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 船舶类型名称: 如集装箱船 */
    private String type;

    /** 总吨。库里列名是大写的 "GT", 所以带引号 */
    @TableField("\"GT\"")
    private BigDecimal gt;

    /** 长度 */
    private BigDecimal length;

    /** 宽度 */
    private BigDecimal width;

    /** 净吨。库里列名是大写的 "NT" */
    @TableField("\"NT\"")
    private BigDecimal nt;

    /** 载重吨。库里列名是大写的 "DWT" */
    @TableField("\"DWT\"")
    private BigDecimal dwt;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
