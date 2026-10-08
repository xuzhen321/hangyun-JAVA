package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.ContainerStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 集装箱状态字典表 container_status 的写操作 + 引用检查。
 */
public interface ContainerStatusMapper extends BaseMapper<ContainerStatus> {

    /**
     * 统计有多少个集装箱在用这个状态。
     * 库里没有物理外键, 删除状态前必须由应用层自己检查引用。
     */
    @Select("select count(*) from container where status_id = #{statusId}")
    long countContainersByStatusId(@Param("statusId") Long statusId);
}
