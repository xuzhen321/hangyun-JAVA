package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.HarborSize;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 港口尺寸字典表 harbor_size 的写操作 + 引用检查。
 */
public interface HarborSizeMapper extends BaseMapper<HarborSize> {

    /** 统计有多少个港口在用这个尺寸。删除前必须由应用层自己检查引用 */
    @Select("select count(*) from port where harbor_size_id = #{harborSizeId}")
    long countPortsByHarborSizeId(@Param("harborSizeId") Long harborSizeId);
}
