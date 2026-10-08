package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.OperationLog;

/**
 * 操作日志表 log 的**写**操作。
 * <p>
 * 查询走视图 v_operation_log（见 {@link OperationLogViewMapper}）。
 * <p>
 * 没有自定义 SQL: 日志只做 INSERT 和分页 SELECT, 用不到手写语句。
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
