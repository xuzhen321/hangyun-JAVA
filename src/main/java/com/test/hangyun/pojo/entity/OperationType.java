package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作类型字典表 operation_type （写用）。
 * <p>
 * ⚠️ 建表语句写的是 {@code create table Operation_Type}, PostgreSQL 会把不加引号的标识符
 * 折成小写, 所以真实表名是 {@code operation_type}。
 * <p>
 * 关系模式: Operation_Type(id, type)。主键 id **自增**(identity)。
 * <p>
 * ⚠️ **1~5 这五个 id 是内置的**, 和枚举 {@link com.test.hangyun.pojo.enums.OpType} 一一对应,
 * 日志切面按 id 写入。它们**不允许删除**, 也不允许改 id(见 Service)。
 * 通过接口新建的类型由自增序列发号, 从 6 开始, 天然不会碰到内置的 1~5。
 */
@Data
@TableName("operation_type")
public class OperationType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 操作类型名称, 如"新增"。库里有唯一约束 */
    private String type;

    /** 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可 */
    private LocalDateTime insertTime;

    /** 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可 */
    private LocalDateTime updateTime;
}
