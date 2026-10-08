package com.test.hangyun.constant;

/**
 * 操作日志表 (log) 的基础数据约定。
 */
public final class OperationLogConstants {

    /** 执行结果: 成功 */
    public static final String RESULT_SUCCESS = "1";

    /** 执行结果: 失败。对应列注释"错误为0, 正确为1" */
    public static final String RESULT_FAIL = "0";

    /** error_message 列是 varchar(500), 写之前必须截断, 否则超长会整条 INSERT 失败 */
    public static final int MAX_ERROR_LENGTH = 500;

    /**
     * after_value / before_value 列的库里类型是 {@code text}(不限长), 但应用层仍做截断。
     * <p>
     * 理由: 审计日志的价值在"谁在什么时候动了哪条记录", 不在把整个请求体存下来。
     * 不截断的话, 一个批量接口的参数能轻松塞进几十 KB, 几十次操作就把表撑起来了。
     */
    public static final int MAX_VALUE_LENGTH = 4000;

    /** 工具类, 不允许实例化 */
    private OperationLogConstants() {
    }
}
