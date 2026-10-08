package com.test.hangyun.pojo.enums;

import lombok.Getter;

/**
 * 操作类型, 与 {@code Operation_Type} 表的五行**一一对应**。
 * <p>
 * 用途: {@code @OpLog(type = OpType.UPDATE)} 声明式地标注"这是个什么操作",
 * 日志切面再拿 {@link #id} 写进 {@code Log.type_id}。
 * <p>
 * ⚠️ **不能通过接口新增或删除类型** —— 这张表是**固定的字典**, 五行就是全部。
 * 原因: 代码里的 {@code @OpLog} 是按 {@link #id} 引用它的, 允许用户删掉一行,
 * 新产生的日志 JOIN 不出类型名(操作日志的"操作类型"列变空白); 允许新增则会让
 * 业务代码依赖一份随时可变的字典数据。
 * <p>
 * ⚠️ {@link #code} 是**写进数据库的原文(英文)**, 必须和 {@code operation-type-data.sql}
 * 里的行完全一致, 改动要两边同步。
 * <p>
 * ⚠️ 特意**显式写出** {@code code} 而不是用 {@code name()}: 万一将来有人重命名枚举常量,
 * {@code name()} 会跟着变, 库里的值就对不上了 —— 而字符串常量不会悄悄变。
 */
@Getter
public enum OpType {

    /** 1 新增(《报告》数据字典原本就有的三个之一) */
    INSERT(1L, "INSERT"),

    /** 2 修改(《报告》数据字典原本就有的三个之一) */
    UPDATE(2L, "UPDATE"),

    /** 3 删除, 含批量删除(《报告》数据字典原本就有的三个之一) */
    DELETE(3L, "DELETE"),

    /** 4 导出 Excel(本次补充: 设计文档 5.4 要求"导出也要留痕") */
    EXPORT(4L, "EXPORT"),

    /** 5 登录(本次补充: 审计需要知道谁什么时候进过系统) */
    LOGIN(5L, "LOGIN");

    /** 写进 Log.type_id 的值, 对应 Operation_Type.id */
    private final long id;

    /** 写进 Operation_Type.type 的原文, 也是操作日志列表里直接展示的文字 */
    private final String code;

    OpType(long id, String code) {
        this.id = id;
        this.code = code;
    }
}
