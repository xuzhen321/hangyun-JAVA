package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表 log （**只写, 几乎不读**）。
 * <p>
 * ⚠️ 类名没叫 {@code Log} 是为了避开两个常见名字: Lombok 的 {@code @Slf4j} 生成的字段
 * 就叫 {@code log}, JDK 里也有 {@code java.util.logging.LogRecord}, 混在一起看代码很别扭。
 * <p>
 * ⚠️ 建表语句写的是 {@code create table Log}, PostgreSQL 会折成小写, 真实表名是 {@code log}。
 * <p>
 * 关系模式: Log(id, user_id, type_id, operation_time, result_status, error_message,
 *                target_table, target_id, before_value, after_value)
 * 主键 id **自增**(identity), 由数据库生成。
 * <p>
 * ⚠️ 这张表**没有逻辑删除**, 也**不提供增删改接口** —— 审计记录只进不出,
 * 允许修改或删除就等于审计失效。列表查询走视图 {@code v_operation_log}。
 */
@Data
@TableName("log")
public class OperationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 操作人ID, 逻辑外键 -> Users.id。取不到当前用户时(如登录前)为 null */
    private Long userId;

    /** 操作类型ID, 逻辑外键 -> Operation_Type.id */
    private Long typeId;

    /** 操作发生时间。应用层显式赋值(不是触发器管的两个时间列) */
    private LocalDateTime operationTime;

    /** 执行结果: '1' 成功, '0' 失败 */
    private String resultStatus;

    /** 失败时的异常信息, 最长 500。超长会被截断 */
    private String errorMessage;

    /** 目标表名, 如 voyage */
    private String targetTable;

    /** 目标记录主键(转成字符串存, 因为有的表主键是字符串) */
    private String targetId;

    /** 操作前的数据快照 */
    private String beforeValue;

    /** 操作后提交的数据快照(方法入参的 JSON) */
    private String afterValue;

    /** 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可 */
    private LocalDateTime insertTime;

    /** 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可 */
    private LocalDateTime updateTime;
}
