package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.CargoType;

/**
 * 货物种类表 cargo_type 的写操作。
 * <p>
 * 引用检查没有做成独立方法: 删除前要拿到"被引用的那几行的名称"来提示用户,
 * 所以直接在 Service 里用 Wrapper 的 inSql 查, 一次拿全(见 CargoTypeServiceImpl)。
 */
public interface CargoTypeMapper extends BaseMapper<CargoType> {
}
