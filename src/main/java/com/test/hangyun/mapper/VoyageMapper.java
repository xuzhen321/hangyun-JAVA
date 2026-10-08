package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Voyage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 航次信息表 voyage 的写操作 + 引用检查。
 */
public interface VoyageMapper extends BaseMapper<Voyage> {

    /**
     * 统计有多少条物流事件挂在这个航次上。
     * 库里没有物理外键, 删除航次前必须由应用层自己检查引用。
     */
    @Select("select count(*) from container_event where voyage_id = #{voyageId}")
    long countEventsByVoyageId(@Param("voyageId") Long voyageId);
}
