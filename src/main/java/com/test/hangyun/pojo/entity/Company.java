package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公司信息表 company 。
 * <p>
 * 这是**多用途字典**: 箱主、操作方、船东、管理公司都用这一张表。
 * 目前被集装箱的 owner_id / operator_id 引用(两个字段都指向它)。
 * <p>
 * `name` 和 `code` 在库里都**没有唯一约束**, 所以允许重名、也允许代码重复。
 * `code` 是箱主代码(BIC 四字码), 也就是箱号的开头四位。
 */
@Data
@TableName("company")
public class Company {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 公司名称 */
    private String name;

    /** 公司代码: 如 CSLU */
    private String code;

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
