package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态响应对象。
 * 与实体分开, 保证接口契约不随表结构变化而波动。
 */
@Data
public class OrderStatusVO {

    private Long id;

    /** 状态描述: 1已确认, 2执行中, 3已完成, 4已取消, 其他自定义 */
    private String description;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static OrderStatusVO from(OrderStatus e) {
        if (e == null) {
            return null;
        }
        OrderStatusVO vo = new OrderStatusVO();
        vo.setId(e.getId());
        vo.setDescription(e.getDescription());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
