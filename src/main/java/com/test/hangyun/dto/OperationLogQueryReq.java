package com.test.hangyun.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志列表查询条件。
 * <p>
 * 每个条件都是可选的, 不传即不参与筛选, 传了才 AND 上去。
 * <p>
 * ⚠️ 日志表**没有逻辑删除**, 不存在"要不要过滤已删除行"的问题。
 */
@Data
public class OperationLogQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 操作人ID, 精确匹配 */
    private Long userId;

    /** 操作类型ID, 精确匹配(用 /operation-types 的下拉框选) */
    private Long operationTypeId;

    /** 目标表名, 精确匹配, 如 voyage */
    private String targetTable;

    /** 执行结果, 精确匹配: '1' 成功 / '0' 失败 */
    private String resultStatus;

    /** 操作时间下界(含), 与 operationTimeTo 组成闭区间 */
    private LocalDateTime operationTimeFrom;

    /** 操作时间上界(含), 与 operationTimeFrom 组成闭区间 */
    private LocalDateTime operationTimeTo;

    /** 关键字, **模糊匹配**: 同时匹配**操作人姓名**和**目标记录主键**, 任一命中即返回 */
    private String keyword;
}
