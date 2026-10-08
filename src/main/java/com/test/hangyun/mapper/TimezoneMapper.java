package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Timezone;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 时区字典表 timezone 的写操作 + 引用检查。
 */
public interface TimezoneMapper extends BaseMapper<Timezone> {

    /** 统计有多少个港口在用这个时区。删除前必须由应用层自己检查引用 */
    @Select("select count(*) from port where timezone_id = #{timezoneId}")
    long countPortsByTimezoneId(@Param("timezoneId") Long timezoneId);
}
