package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Country;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 国家字典表 country 的写操作 + 引用检查。
 */
public interface CountryMapper extends BaseMapper<Country> {

    /**
     * 统计有多少条记录在用这个国家。
     * <p>
     * ⚠️ **两处引用都要查**: `vessel.country_id`(船旗国) 和 `port.country_id`(港口所在国家)。
     * 只查一处的话, 另一个被引用着的国家会被误删。
     * 库里没有物理外键, 删除前必须由应用层自己检查。
     */
    @Select("select (select count(*) from vessel where country_id = #{countryId})"
            + "     + (select count(*) from port   where country_id = #{countryId})")
    long countReferences(@Param("countryId") Long countryId);
}
