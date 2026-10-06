package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Order;

/**
 * 订单表 orders 的写操作。
 * <p>
 * 没有引用检查方法: 订单现在是逻辑删除(把 status_id 置为 4 已取消, 见 OrderServiceImpl#delete),
 * 数据行还在, Cargo 之类的引用不会悬空。
 */
public interface OrderMapper extends BaseMapper<Order> {
}
