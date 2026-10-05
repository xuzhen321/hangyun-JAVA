package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Customer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户表 customer 的写操作 + 引用检查。
 */
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 统计该客户下的订单数量。
     * 库里没有物理外键, 删除客户前必须由应用层自己检查引用。
     */
    @Select("select count(*) from orders where customer_id = #{customerId}")
    long countOrdersByCustomerId(@Param("customerId") Long customerId);
}
