package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.test.hangyun.pojo.enums.EstimateFlag;
import com.test.hangyun.pojo.enums.EventSource;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱物流事件表 container_event （写用）。
 * <p>
 * 报告中的关系模式: Container_Event(id, container_no, source, voyage_id,
 *                                   event_place_id, event_status_id, is_esti, event_time)
 * 语义: 某个集装箱在某个时间、某个地点发生的一个物流节点事件。
 * <p>
 * 两个 char(1) 字段用枚举映射(source / isEsti), 库里存 '1'/'0'、'Y'/'N', 接口层给中文。
 */
@Data
@TableName("container_event")
public class ContainerEvent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 箱号, 逻辑外键 -> container.no */
    private String containerNo;

    /** 数据来源: 船公司 / 港区(见 EventSource) */
    private EventSource source;

    /** 航次ID, 逻辑外键 -> voyage.id */
    private Long voyageId;

    /** 发生地ID, 逻辑外键 -> port.id */
    private Long eventPlaceId;

    /** 事件状态ID, 逻辑外键 -> event_status.id */
    private Long eventStatusId;

    /** 实际发生 / 预计发生(见 EstimateFlag) */
    private EstimateFlag isEsti;

    /** 状态发生时间 */
    private LocalDateTime eventTime;

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
