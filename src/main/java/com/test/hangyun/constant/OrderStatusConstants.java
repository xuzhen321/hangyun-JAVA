package com.test.hangyun.constant;

/**
 * 订单状态的基础数据约定。
 * <p>
 * id 1/2/3/4 是系统初始化时写入的四条基础状态(见 order-status-data.sql), 语义固定,
 * 业务上既**不允许删除**也**不允许修改** —— 订单表的 status_id 会引用它们, 删掉会让已存在的
 * 订单失去状态; 改描述则会让 id 与含义脱钩, 而业务代码是按 id 判断的。
 * <p>
 * 其中 4(已取消)还被"订单逻辑删除"复用: 订单列表的删除操作只是把 status_id 改成 4,
 * 不真的删行, 详见 {@code OrderServiceImpl#delete}。
 */
public final class OrderStatusConstants {

    /** 已确认 */
    public static final long STATUS_CONFIRMED = 1L;

    /** 执行中 */
    public static final long STATUS_IN_PROGRESS = 2L;

    /** 已完成 */
    public static final long STATUS_COMPLETED = 3L;

    /** 已取消。订单逻辑删除就是改成这个状态 */
    public static final long STATUS_CANCELLED = 4L;

    /** 内置状态的最小 id */
    public static final long BUILTIN_MIN_ID = STATUS_CONFIRMED;

    /** 内置状态的最大 id */
    public static final long BUILTIN_MAX_ID = STATUS_CANCELLED;

    /** 工具类, 不允许实例化 */
    private OrderStatusConstants() {
    }

    /** 是否属于不允许删除的内置状态 */
    public static boolean isBuiltin(Long id) {
        return id != null && id >= BUILTIN_MIN_ID && id <= BUILTIN_MAX_ID;
    }
}
