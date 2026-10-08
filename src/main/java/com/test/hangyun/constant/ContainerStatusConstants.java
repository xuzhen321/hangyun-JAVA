package com.test.hangyun.constant;

/**
 * 集装箱状态的基础数据约定。
 * <p>
 * ⚠️ 和客户状态 / 订单状态**不一样**: 那两张表是"1-N 整段都是内置的"，
 * 而集装箱状态里**只有 {@link #STATUS_DELETED}（id=7）是内置的** ——
 * 因为只有它被代码依赖（集装箱逻辑删除的落点）。
 * 其余几条（1-6，在途 / 在堆场 / …）都是普通字典项, 只要没被集装箱引用就能随便改、随便删。
 */
public final class ContainerStatusConstants {

    /**
     * 已删除。
     * <p>
     * 集装箱的"删除"不真删数据行, 只把 status_id 改成这个值 ——
     * 这样装箱结果等历史记录还能查到, 引用也不会悬空。
     * 与 seed 数据 container-status-data.sql 里写死的 id 一一对应, 不许改。
     */
    public static final long STATUS_DELETED = 7L;

    /** 工具类, 不允许实例化 */
    private ContainerStatusConstants() {
    }

    /** 是否是被代码依赖的内置状态（目前只有"已删除"一个） */
    public static boolean isBuiltin(Long statusId) {
        return statusId != null && statusId == STATUS_DELETED;
    }

    /** 是否已删除 */
    public static boolean isDeleted(Long statusId) {
        return isBuiltin(statusId);
    }
}
