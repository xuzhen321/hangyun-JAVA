package com.test.hangyun.constant;

/**
 * 港口表的基础数据约定。
 */
public final class PortConstants {

    /**
     * port.state 的"已删除"取值。
     * <p>
     * 见 initial.sql 的列注释: 0默认, 1新增, 2修改, 3删除 —— 端口是**逻辑删除**,
     * 所以查询接口必须手工排除 state = '3' 的行(文档 3.6 专门提醒过这一点)。
     */
    public static final String STATE_DELETED = "3";

    /** 工具类, 不允许实例化 */
    private PortConstants() {
    }
}
