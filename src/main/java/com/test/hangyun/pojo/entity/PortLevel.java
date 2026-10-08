package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 港口级别字典表 port_level 。被 port.level_id 引用。字段没有唯一约束。
 */
@Data
@TableName("port_level")
public class PortLevel {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 港口级别 */
    private Integer level;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
