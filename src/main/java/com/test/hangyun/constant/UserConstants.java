package com.test.hangyun.constant;

/**
 * 用户表 (Users) 的基础数据约定。
 * <p>
 * {@code Users.status} 的取值: {@code 0} 正常 / {@code 1} 冻结 / {@code 2} 已删除。
 * <p>
 * 其中 {@link #STATUS_DELETED} 还被"用户逻辑删除"复用: 用户列表的删除操作只是把
 * {@code status} 改成 {@code '2'}, 不真的删行, 详见 {@code UserServiceImpl#delete}。
 * <p>
 * ⚠️ 只有"正常"的账号能登录 —— 冻结和已删除一律拒绝(见 {@link #canLogin})。
 * <p>
 * ⚠️ 逻辑删除的行**仍然占用 {@code username} 的唯一约束**: {@code Users.username} 是 UNIQUE,
 * 删掉(其实是标记)之后那一行还在表里, 所以**同名用户名无法重新注册**。
 */
public final class UserConstants {

    /** 正常。只有这个状态能登录 */
    public static final String STATUS_NORMAL = "0";

    /** 冻结。账号还在, 但登录会被拒 */
    public static final String STATUS_FROZEN = "1";

    /** 已删除。用户逻辑删除的落点 */
    public static final String STATUS_DELETED = "2";

    /**
     * 初始管理员的 id, 见 user-data.sql。
     * <p>
     * 不允许删除, 也不允许改成"冻结/已删除" —— 否则把唯一的管理员锁死之后,
     * 没人能再登录进来改回去。修改密码、改姓名电话不受限制。
     */
    public static final long BUILTIN_ADMIN_ID = 1L;

    /** 能否登录: 必须正好是"正常" */
    public static boolean canLogin(String status) {
        return STATUS_NORMAL.equals(status);
    }

    /** 是否为"已删除"(逻辑删除) */
    public static boolean isDeleted(String status) {
        return STATUS_DELETED.equals(status);
    }

    /** 是否为内置管理员账号 */
    public static boolean isBuiltinAdmin(Long id) {
        return id != null && id == BUILTIN_ADMIN_ID;
    }

    /** 工具类, 不允许实例化 */
    private UserConstants() {
    }
}
