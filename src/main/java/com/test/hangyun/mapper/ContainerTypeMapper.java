package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.ContainerType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 集装箱箱型字典表 container_type 的写操作 + 引用检查。
 */
public interface ContainerTypeMapper extends BaseMapper<ContainerType> {

    /**
     * 统计有多少个集装箱在用这个箱型。
     * 库里没有物理外键, 删除箱型前必须由应用层自己检查引用。
     */
    @Select("select count(*) from container where type_id = #{typeId}")
    long countContainersByTypeId(@Param("typeId") Long typeId);
}
