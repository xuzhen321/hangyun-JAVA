package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Port;

/**
 * 港口表 port 。
 * <p>
 * 目前只服务于订单的港口引用校验, 没有对应的 Controller/Service,
 * /ports 接口开始做时再补。
 */
public interface PortMapper extends BaseMapper<Port> {
}
