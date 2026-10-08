package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱箱型字典表 container_type 。
 * <p>
 * 箱型由"类别 + 尺寸"两个字段组成, 比如 type="普通箱" + size="40英尺"。
 * 两个字段在库里都没有唯一约束, 所以允许出现重复的箱型。
 */
@Data
@TableName("container_type")
public class ContainerType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 箱型类别: 如普通箱、高箱、冷藏箱 */
    private String type;

    /** 箱尺寸: 如20英尺、40英尺 */
    private String size;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
