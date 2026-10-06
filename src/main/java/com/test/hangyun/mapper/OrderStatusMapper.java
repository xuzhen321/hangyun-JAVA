package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.OrderStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单状态字典表 order_status 的写操作 + 引用检查。
 */
public interface OrderStatusMapper extends BaseMapper<OrderStatus> {

    /**
     * 统计引用该状态的订单数量。
     * 库里没有物理外键, 删除状态前必须由应用层自己检查引用。
     */
    @Select("select count(*) from orders where status_id = #{statusId}")
    long countOrdersByStatusId(@Param("statusId") Long statusId);
}
