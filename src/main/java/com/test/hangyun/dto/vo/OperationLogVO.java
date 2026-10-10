package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.view.OperationLogView;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志响应对象。
 * <p>
 * 操作人姓名和操作类型名称都来自**视图**, 不用 Service 再查一次。
 */
@Data
public class OperationLogVO {

    /** 日志ID */
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人姓名(视图联出来的, 可能为 null —— 比如操作人已被删) */
    private String operatorName;

    /** 操作类型ID */
    private Long operationTypeId;

    /** 操作类型名称, 如"新增" */
    private String operationType;

    /** 目标表名 */
    private String targetTable;

    /** 目标记录主键 */
    private String targetId;

    /** 操作发生时间 */
    private LocalDateTime operationTime;

    /**
     * 操作前的数据快照(改之前那一整行的 JSON)。
     * <p>
     * ⚠️ 只有 UPDATE / DELETE 且目标表挂了触发器时才有值 —— 应用层切面写不出它
     * (拿不到"改之前"的快照), 这一列由数据库触发器 {@code log_row_change()} 填,
     * 见 log-before-trigger.sql。INSERT / EXPORT / LOGIN 恒为 null。
     */
    private String beforeValue;

    /** 操作后提交的数据快照(方法入参的 JSON) */
    private String afterValue;

    /** 执行结果: '1' 成功 / '0' 失败 */
    private String resultStatus;

    /** 失败时的异常信息 */
    private String errorMessage;

    public static OperationLogVO from(OperationLogView v) {
        if (v == null) {
            return null;
        }
        OperationLogVO vo = new OperationLogVO();
        vo.setId(v.getLogId());
        vo.setUserId(v.getUserId());
        vo.setOperatorName(v.getOperatorName());
        vo.setOperationTypeId(v.getOperationTypeId());
        vo.setOperationType(v.getOperationType());
        vo.setTargetTable(v.getTargetTable());
        vo.setTargetId(v.getTargetId());
        vo.setOperationTime(v.getOperationTime());
        vo.setBeforeValue(v.getBeforeValue());
        vo.setAfterValue(v.getAfterValue());
        vo.setResultStatus(v.getResultStatus());
        vo.setErrorMessage(v.getErrorMessage());
        return vo;
    }
}
