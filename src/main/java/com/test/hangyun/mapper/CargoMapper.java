package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Cargo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 货物信息表 cargo 。
 * <p>
 * 库里**没有** v_cargo 视图(视图设计阶段去掉了), 所以这个资源直接读基础表。
 * 需要带出货物种类名称时, 由 Service 层再查一次组装, 见 CargoServiceImpl。
 */
public interface CargoMapper extends BaseMapper<Cargo> {

    /**
     * 统计这批货物里有多少条已被装箱结果引用。
     * <p>
     * cargo_container_result.cargo_id 逻辑引用 cargo.id, 库里没有物理外键,
     * 所以物理删除前必须由应用层自己检查引用, 否则会留下悬空的 cargo_id。
     * <p>
     * 用 count(distinct cargo_id) 而不是 count(*): 我们要的是"有几条货物被引用",
     * 一条货物可能对应多条装箱记录。
     */
    @Select("<script>"
            + "select count(distinct cargo_id) from cargo_container_result "
            + "where cargo_id in "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    long countReferencedByIds(@Param("ids") List<Long> ids);
}
