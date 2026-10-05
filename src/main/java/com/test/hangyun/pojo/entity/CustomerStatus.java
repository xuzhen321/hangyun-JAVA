package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户状态字典表 customer_status 。
 * description 取值: 1正常, 2异常, 3注销, 其他自定义。
 */
@Data
@TableName("customer_status")
public class CustomerStatus {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String description;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
