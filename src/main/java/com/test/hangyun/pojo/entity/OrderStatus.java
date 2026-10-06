package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态字典表 order_status 。
 * description 取值: 1已确认, 2执行中, 3已完成, 4已取消, 其他自定义。
 */
@Data
@TableName("order_status")
public class OrderStatus {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String description;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
