package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.PortType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 港口类型字典表 port_type 的写操作 + 引用检查。
 */
public interface PortTypeMapper extends BaseMapper<PortType> {

    /** 统计有多少个港口在用这个类型。删除前必须由应用层自己检查引用 */
    @Select("select count(*) from port where port_type_id = #{portTypeId}")
    long countPortsByPortTypeId(@Param("portTypeId") Long portTypeId);
}
