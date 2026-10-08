package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Container;

/**
 * 集装箱信息表 container 。
 * <p>
 * 库里**没有** v_container 视图(视图设计阶段去掉了), 所以直接读基础表。
 * 箱型、箱主、操作方、状态的名称由 Service 层分步查询组装。
 * <p>
 * 这里没有"引用检查"方法: 集装箱现在是**逻辑删除**(把 status_id 置为 7 已删除),
 * 数据行还在, 装箱结果之类的引用不会悬空, 所以删除前不需要检查引用。
 */
public interface ContainerMapper extends BaseMapper<Container> {
}
