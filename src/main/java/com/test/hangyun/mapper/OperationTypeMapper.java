package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.OperationType;

/**
 * 操作类型字典表 operation_type 的读写。
 * <p>
 * 没有自定义 SQL: 这张表**不支持新增和删除**(见 OperationTypeController),
 * 所以既不需要唯一性之外的特殊查询, 也不需要"删除前查引用"的统计。
 */
public interface OperationTypeMapper extends BaseMapper<OperationType> {
}
