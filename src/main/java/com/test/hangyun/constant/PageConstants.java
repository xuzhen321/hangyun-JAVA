package com.test.hangyun.constant;

/**
 * 分页参数约定。
 * <p>
 * 各资源的分页接口共用同一套默认值与上限, 集中放这里, 避免每个 Service 各写一份
 * (之前 CustomerServiceImpl 和 CustomerStatusServiceImpl 里就是重复的)。
 */
public final class PageConstants {

    /** 未传 size 时的每页条数 */
    public static final long DEFAULT_SIZE = 10;

    /** 每页条数上限, 超出按上限截断 */
    public static final long MAX_SIZE = 100;

    /** 工具类, 不允许实例化 */
    private PageConstants() {
    }

    /** 页码规整: 为空或小于 1 一律按第 1 页 */
    public static long normalizePage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    /** 每页条数规整: 为空或小于 1 取默认值, 超过上限按上限截断 */
    public static long normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
