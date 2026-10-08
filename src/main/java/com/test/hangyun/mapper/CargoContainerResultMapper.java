package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.CargoContainerResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 货物装箱结果表 cargo_container_result 。
 * <p>
 * 库里没有对应的视图, 所以直接读基础表。
 * "货物名称"要跳两跳(cargo_container_result -> cargo -> cargo_type),
 * 由 Service 层分步查询组装, 见 CargoContainerResultServiceImpl。
 */
public interface CargoContainerResultMapper extends BaseMapper<CargoContainerResult> {

    /**
     * 某批货物已经装箱的总量。
     * <p>
     * 用来校验"装箱总量不能超过该货物的数量"(cargo.quantity)。
     * <p>
     * 必须用 coalesce 兜底: 一条装箱记录都没有时 SUM 返回的是 **NULL** 而不是 0,
     * 直接映射成 long 会抛异常。
     */
    @Select("select coalesce(sum(quantity), 0) from cargo_container_result where cargo_id = #{cargoId}")
    long sumQuantityByCargoId(@Param("cargoId") Long cargoId);
}
