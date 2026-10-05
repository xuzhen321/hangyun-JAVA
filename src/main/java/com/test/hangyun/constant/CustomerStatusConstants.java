package com.test.hangyun.constant;

/**
 * 客户状态的基础数据约定。
 * <p>
 * id 1/2/3 是系统初始化时写入的三条基础状态(见 customer-status-data.sql), 语义固定, 业务上不允许删除 ——
 * 客户表的 status_id 会引用它们, 删掉会让已存在的客户失去状态。
 * <p>
 * 其中 3(注销)还被"客户逻辑删除"复用: 客户列表的删除操作只是把 status_id 改成 3,
 * 不真的删行, 详见 {@code CustomerServiceImpl#delete}。
 */
public final class CustomerStatusConstants {

    /** 正常 */
    public static final long STATUS_NORMAL = 1L;

    /** 异常 */
    public static final long STATUS_ABNORMAL = 2L;

    /** 注销。客户逻辑删除就是改成这个状态 */
    public static final long STATUS_CANCELLED = 3L;

    /** 内置状态的最小 id */
    public static final long BUILTIN_MIN_ID = STATUS_NORMAL;

    /** 内置状态的最大 id */
    public static final long BUILTIN_MAX_ID = STATUS_CANCELLED;

    /** 工具类, 不允许实例化 */
    private CustomerStatusConstants() {
    }

    /** 是否属于不允许删除的内置状态 */
    public static boolean isBuiltin(Long id) {
        return id != null && id >= BUILTIN_MIN_ID && id <= BUILTIN_MAX_ID;
    }
}
