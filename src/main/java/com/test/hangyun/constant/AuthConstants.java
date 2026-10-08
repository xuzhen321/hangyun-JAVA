package com.test.hangyun.constant;

/**
 * 登录令牌(JWT)相关的约定。
 * <p>
 * 免校验路径(白名单)**不在这里** —— 它写在 {@code WebMvcConfig#addInterceptors} 里,
 * 用 Spring 的 {@code excludePathPatterns} 声明, 比在拦截器里手工比对路径清晰。
 */
public final class AuthConstants {

    /** 令牌所在的请求头 */
    public static final String HEADER = "Authorization";

    /** 令牌前缀。**注意末尾这个空格**, 拼的是 {@code "Bearer xxx"} */
    public static final String BEARER_PREFIX = "Bearer ";

    /** 令牌 payload 里存用户 id 的 claim 名 */
    public static final String CLAIM_USER_ID = "userId";

    /** 令牌 payload 里存登录名的 claim 名 */
    public static final String CLAIM_USERNAME = "username";

    /**
     * 令牌缺失/无效/过期时的提示文案。
     * <p>
     * 对应 HTTP {@code 401} + {@code code=0} —— 前端拦截器看到 401 就跳登录页。
     * <p>
     * ⚠️ 三种情况(el 头没带 / 格式不对 / 过期)**故意用同一句话**: 区分开等于告诉
     * 攻击者"这个令牌是过期的、那个是伪造的", 对正常用户没有价值。
     */
    public static final String MSG_UNAUTHORIZED = "登录已失效，请重新登录";

    /** 工具类, 不允许实例化 */
    private AuthConstants() {
    }
}
