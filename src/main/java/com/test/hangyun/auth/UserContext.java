package com.test.hangyun.auth;

/**
 * 当前请求的登录用户, 用 {@link ThreadLocal} 在整条请求链上传递。
 * <p>
 * 由 {@link AuthInterceptor} 在校验令牌通过后写入, 在请求结束时清除。
 * <p>
 * ⚠️ **必须清除, 而且要在 {@code afterCompletion} 里清**(成功失败都要走那里)。
 * Tomcat 的工作线程是**复用**的, 不清的话下一个请求会读到上一个用户的身份 ——
 * 表现是"莫名其妙记成了别人操作的", 而且只在并发/复用时偶发, 极难排查。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    /** 校验通过后由拦截器写入 */
    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    /** 当前登录用户; 未登录(白名单路径)时为 null */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 当前登录用户 id; 未登录时为 null。日志切面用它写 Log.user_id */
    public static Long currentUserId() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.id();
    }

    /** 请求结束时必须调用, 见类注释 */
    public static void clear() {
        HOLDER.remove();
    }

    /** 工具类, 不允许实例化 */
    private UserContext() {
    }
}
