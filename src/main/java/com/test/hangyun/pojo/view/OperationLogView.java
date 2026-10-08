package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志视图 v_operation_log （只读）。
 * <p>
 * 视图把 {@code Users} 和 {@code Operation_Type} 都联好了, 所以列表**不用 Service 二次组装**:
 * 操作人姓名({@code operatorName})和操作类型名({@code operationType})直接就在视图里。
 * <p>
 * ⚠️ 视图里主键列叫 {@code log_id}, 类型 id 列叫 {@code operation_type_id}
 * (因为 {@code operation_type} 这个名字被类型**名称**占了)。
 */
@Data
@TableName("v_operation_log")
public class OperationLogView {

    /** 视图里主键列叫 log_id */
    @TableId(value = "log_id", type = IdType.INPUT)
    private Long logId;

    /** 操作人ID */
    private Long userId;

    /** 操作人姓名, 来自 Users.real_name */
    private String operatorName;

    /** 操作类型ID */
    private Long operationTypeId;

    /** 操作类型名称, 来自 Operation_Type.type */
    private String operationType;

    /** 目标表名 */
    private String targetTable;

    /** 目标记录主键 */
    private String targetId;

    /** 操作发生时间 */
    private LocalDateTime operationTime;

    /** 操作前的数据快照 */
    private String beforeValue;

    /** 操作后提交的数据快照 */
    private String afterValue;

    /** 执行结果: '1' 成功, '0' 失败 */
    private String resultStatus;

    /** 失败时的异常信息 */
    private String errorMessage;
}
