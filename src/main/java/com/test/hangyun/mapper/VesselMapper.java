package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Vessel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 船舶信息表 vessel 的写操作 + 引用检查。
 * <p>
 * 查询走视图 v_vessel（见 VesselViewMapper）—— 名称类字段视图都联好了。
 */
public interface VesselMapper extends BaseMapper<Vessel> {

    /**
     * 统计有多少个航次用了这艘船。
     * 库里没有物理外键, 删除船舶前必须由应用层自己检查引用。
     */
    @Select("select count(*) from voyage where vsl_id = #{vesselId}")
    long countVoyagesByVesselId(@Param("vesselId") Long vesselId);

    /**
     * 从这批船舶 id 里挑出「已被航次引用」的那些（也就是不能删的）。
     * 批量删除时用它一次查出拦截名单, 再去查船名做提示。
     */
    @Select("<script>"
            + "select distinct vsl_id from voyage where vsl_id in "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<Long> findReferencedIds(@Param("ids") List<Long> ids);
}
