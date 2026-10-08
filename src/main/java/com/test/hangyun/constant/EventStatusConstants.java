package com.test.hangyun.constant;

/**
 * 集装箱事件状态的基础数据约定。
 * <p>
 * 只有 {@link #STATUS_DELETED}（id=6）是内置的 —— 因为只有它被代码依赖
 * （集装箱事件逻辑删除的落点）。其余几条（1-5，预安排 / 装船中 / …）都是普通字典项,
 * 只要没被事件引用就能随便改、随便删。这点和集装箱状态(ContainerStatusConstants)一致。
 * <p>
 * 语义: 1预安排 2装船中 3航行中 4卸货中 5已完成 6已删除。
 */
public final class EventStatusConstants {

    /**
     * 已删除。
     * <p>
     * 集装箱事件的"删除"不真删数据行, 只把 event_status_id 改成这个值 ——
     * 这样物流轨迹等历史记录还能查到, 引用也不会悬空。
     * 与 seed 数据 event-status-data.sql 里写死的 id 一一对应, 不许改。
     */
    public static final long STATUS_DELETED = 6L;

    /** 工具类, 不允许实例化 */
    private EventStatusConstants() {
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
