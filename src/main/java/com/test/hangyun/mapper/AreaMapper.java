package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Area;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 区域字典表 area 的写操作 + 引用检查。
 */
public interface AreaMapper extends BaseMapper<Area> {

    /** 统计有多少个港口在这个区域。删除区域前必须由应用层自己检查引用 */
    @Select("select count(*) from port where area_id = #{areaId}")
    long countPortsByAreaId(@Param("areaId") Long areaId);
}
