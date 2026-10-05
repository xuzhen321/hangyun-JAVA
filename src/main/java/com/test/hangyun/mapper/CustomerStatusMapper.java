package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.CustomerStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户状态字典表 customer_status 的写操作 + 引用检查。
 */
public interface CustomerStatusMapper extends BaseMapper<CustomerStatus> {

    /**
     * 统计引用该状态的客户数量。
     * 库里没有物理外键, 删除状态前必须由应用层自己检查引用。
     */
    @Select("select count(*) from customer where status_id = #{statusId}")
    long countCustomersByStatusId(@Param("statusId") Long statusId);
}
