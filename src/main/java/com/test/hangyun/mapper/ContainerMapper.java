package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Container;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 集装箱信息表 container 。
 * <p>
 * 库里**没有** v_container 视图(视图设计阶段去掉了), 所以直接读基础表。
 * 箱型、箱主、操作方、状态的名称由 Service 层分步查询组装。
 */
public interface ContainerMapper extends BaseMapper<Container> {

    /**
     * 从这批箱号里挑出「已被装箱结果引用」的那些。
     * <p>
     * cargo_container_result.container_no 逻辑引用 container.no, 库里没有物理外键,
     * 所以物理删除前必须由应用层自己检查引用, 否则会留下悬空的箱号。
     * 一次查出全部被引用的箱号, 既用来判断"能不能删", 也用来在提示里说清是哪些箱。
     */
    @Select("<script>"
            + "select distinct container_no from cargo_container_result "
            + "where container_no in "
            + "<foreach collection='nos' item='no' open='(' separator=',' close=')'>#{no}</foreach>"
            + "</script>")
    List<String> findReferencedNos(@Param("nos") List<String> nos);
}
