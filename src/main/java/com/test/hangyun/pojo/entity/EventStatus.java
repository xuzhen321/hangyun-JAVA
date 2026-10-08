package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱事件状态字典表 event_status 。
 * <p>
 * 和集装箱状态一样有两列描述(中英文各一列), 两列都有唯一约束。
 * 内置值: 1预安排 2装船中 3航行中 4卸货中 5已完成 6已删除(见 event-status-data.sql)。
 */
@Data
@TableName("event_status")
public class EventStatus {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 状态中文描述 */
    private String descriptionCn;

    /** 状态英文描述 */
    private String descriptionEn;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
