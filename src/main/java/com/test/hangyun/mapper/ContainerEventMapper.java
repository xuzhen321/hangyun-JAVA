package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.ContainerEvent;

/**
 * 集装箱物流事件表 container_event 的写操作。
 * <p>
 * 没有引用检查方法: 事件现在是**逻辑删除**(把 event_status_id 置为 6 已删除),
 * 数据行还在, 引用不会悬空。
 */
public interface ContainerEventMapper extends BaseMapper<ContainerEvent> {
}
