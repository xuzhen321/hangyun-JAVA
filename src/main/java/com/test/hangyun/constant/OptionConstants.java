package com.test.hangyun.constant;

/**
 * 下拉框候选（options 类接口）的通用约定。
 * <p>
 * 客户、港口等"输入前缀 → 返回候选列表"的接口共用同一套口径,
 * 集中放这里, 避免每个模块各写一份(和之前分页参数的情况一样)。
 */
public final class OptionConstants {

    /**
     * 候选列表最多返回多少条。
     * <p>
     * 下拉框是给人选的, 返回几百条没意义还拖慢请求; 前端输入前缀后能缩到很小的候选集,
     * 所以固定 20 条足够。注意这不是分页 —— options 接口没有 total, 也不接受 size 参数。
     */
    public static final int OPTION_LIMIT = 20;

    /** 工具类, 不允许实例化 */
    private OptionConstants() {
    }
}
