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
 * 主键是自增的 id。
 * <p>
 * ⚠️ 航次号 no **本身不是唯一键** —— 航次号由船公司自编, 跨公司重号是常态。
 * 库里唯一的是 **(vsl_id, no) 这一对**(见 initial.sql 的 uq_voyage_vsl_no):
 * 同一艘船不能有两个同号航次, 不同船可以重号。应用层在新增/修改时做同样的查重, 返回 409。
 * <p>
 * vsl_id 指向 vessel, loading/discharge_port_id 指向 port, 库里都没有物理外键,
 * 存在性由 Service 层校验。
 */
@Data
@TableName("voyage")
public class Voyage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 航次号, 如 2026E001 */
    private String no;

    /** 船舶ID, 逻辑外键 -> vessel.id */
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
