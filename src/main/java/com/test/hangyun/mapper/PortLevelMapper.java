package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.PortLevel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 港口级别字典表 port_level 的写操作 + 引用检查。
 */
public interface PortLevelMapper extends BaseMapper<PortLevel> {

    /** 统计有多少个港口在用这个级别。删除前必须由应用层自己检查引用 */
    @Select("select count(*) from port where level_id = #{levelId}")
    long countPortsByLevelId(@Param("levelId") Long levelId);
}
