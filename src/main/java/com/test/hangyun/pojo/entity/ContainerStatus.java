package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱状态字典表 container_status 。
 * <p>
 * ⚠️ 和客户状态 / 订单状态**不一样**: 这张表有两列描述(中英文各一列),
 * 而且**两列都有唯一约束** —— 所以新增/修改时要分别查重。
 */
@Data
@TableName("container_status")
public class ContainerStatus {

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
