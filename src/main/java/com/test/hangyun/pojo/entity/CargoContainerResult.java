package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货物装箱结果表 cargo_container_result 。
 * <p>
 * 报告中的关系模式: Cargo_Container_Result(id, cargo_id, container_no, quantity)
 * 语义: "哪一批货物装进了哪个集装箱、装了多少"。
 * <p>
 * 三列都是 not null(见 initial.sql): 这条记录缺了货物、箱号或数量任意一个都没有意义。
 */
@Data
@TableName("cargo_container_result")
public class CargoContainerResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 货物ID, 逻辑外键 -> cargo.id */
    private Long cargoId;

    /** 集装箱号, 逻辑外键 -> container.no (varchar) */
    private String containerNo;

    /** 装入数量 */
    private Integer quantity;

    /**
     * 插入时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime insertTime;

    /**
     * 修改时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime updateTime;
}
