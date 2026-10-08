package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.ShipType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 船舶类型字典表 ship_type 的写操作 + 引用检查。
 */
public interface ShipTypeMapper extends BaseMapper<ShipType> {

    /**
     * 统计有多少艘船在用这个船型。
     * 库里没有物理外键, 删除船型前必须由应用层自己检查引用。
     */
    @Select("select count(*) from vessel where vessel_type_id = #{typeId}")
    long countVesselsByTypeId(@Param("typeId") Long typeId);
}
