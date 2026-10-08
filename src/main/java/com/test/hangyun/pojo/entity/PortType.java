package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 港口类型字典表 port_type 。被 port.port_type_id 引用。字段没有唯一约束。
 * type 取值注释: 1系统规范港口, 2用户自定义港口。
 */
@Data
@TableName("port_type")
public class PortType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 港口类型: 1系统规范港口, 2用户自定义港口 */
    private String type;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
