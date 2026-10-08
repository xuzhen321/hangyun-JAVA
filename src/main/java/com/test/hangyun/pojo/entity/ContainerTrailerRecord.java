package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱拖车记录表 container_trailer_record （提空箱登记）。
 * <p>
 * 报告中的关系模式: Container_Trailer_Record(id, container_no, track_no,
 *                                          dc_in_date, dc_out_date, use_note)
 * 语义: 哪个拖车（`track_no`）把哪个空箱（`container_no`）提进场、什么时候出场。
 */
@Data
@TableName("container_trailer_record")
public class ContainerTrailerRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 箱号, 逻辑外键 -> container.no */
    private String containerNo;

    /** 提空箱拖车号, 逻辑外键 -> trailer.no */
    private String trackNo;

    /** 提空箱拖车进场时间 */
    private LocalDateTime dcInDate;

    /** 提空箱拖车出场时间 */
    private LocalDateTime dcOutDate;

    /** 箱使用备注 */
    private String useNote;

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
