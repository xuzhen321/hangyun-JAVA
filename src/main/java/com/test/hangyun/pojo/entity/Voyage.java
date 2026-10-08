package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 航次信息表 voyage 。
 * <p>
 * 报告中的关系模式: Voyage(id, no, vsl_id, loading_port_id, discharge_port_id)
 * 主键是自增的 id, 航次号 no 只是一个业务字段(库里没有唯一约束)。
 * <p>
 * ⚠️ vsl_id 指向 vessel, 但**船舶模块还没做**(第 6 个模块), 而且 vessel 表是空的 ——
 * 所以这个字段目前**不做存在性校验**(校验了会挡住所有航次的创建)。
 * 等船舶模块做出来再补。
 */
@Data
@TableName("voyage")
public class Voyage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 航次号, 如 2026E001 */
    private String no;

    /** 船舶ID, 逻辑外键 -> vessel.id (目前不校验, 见类注释) */
    private Long vslId;

    /** 起始港口ID, 逻辑外键 -> port.id */
    private Long loadingPortId;

    /** 目的港口ID, 逻辑外键 -> port.id */
    private Long dischargePortId;

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
