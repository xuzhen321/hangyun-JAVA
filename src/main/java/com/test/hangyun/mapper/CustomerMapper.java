package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Customer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户表 customer 的写操作, 外加一个统计查询。
 */
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 统计该客户下的订单数量。
     * <p>
     * 注意: 客户现在是**逻辑删除**(把 status_id 置为 3 注销, 见 CustomerServiceImpl#delete),
     * 数据行还在, 订单不会悬空, 所以删除前不再需要做这道引用检查 —— 本方法目前没有调用方,
     * 保留供统计/展示使用(如详情页显示"该客户共有 N 个订单")。
     */
    @Select("select count(*) from orders where customer_id = #{customerId}")
    long countOrdersByCustomerId(@Param("customerId") Long customerId);
}
