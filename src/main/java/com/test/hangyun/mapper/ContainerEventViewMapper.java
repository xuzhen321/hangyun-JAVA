package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.view.ContainerEventView;

/**
 * 集装箱物流事件视图 v_container_event （只读）。
 * <p>
 * 列表、详情、轨迹查询都走这个视图 —— 船名/航次/发生地/状态描述它已经联好了。
 */
public interface ContainerEventViewMapper extends BaseMapper<ContainerEventView> {
}
