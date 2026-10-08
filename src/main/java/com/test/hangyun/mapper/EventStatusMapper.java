package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.EventStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 集装箱事件状态字典表 event_status 的写操作 + 引用检查。
 */
public interface EventStatusMapper extends BaseMapper<EventStatus> {

    /**
     * 统计有多少条事件在用这个状态。
     * 库里没有物理外键, 删除状态前必须由应用层自己检查引用。
     */
    @Select("select count(*) from container_event where event_status_id = #{statusId}")
    long countEventsByStatusId(@Param("statusId") Long statusId);
}
