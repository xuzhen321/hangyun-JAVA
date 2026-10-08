package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域字典表 area 。被 port.area_id 引用。
 * area_name 在库里有唯一约束。
 */
@Data
@TableName("area")
public class Area {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 区域名称: 如 North China。库里有唯一约束 */
    private String areaName;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
