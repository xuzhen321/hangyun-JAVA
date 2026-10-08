package com.test.hangyun.auth;

/**
 * 当前登录用户 —— 从 JWT 里解析出来的身份。
 * <p>
 * 只带令牌里**确实有的**两个字段(id / 登录名)。真实姓名、电话这些不在令牌里,
 * 需要的话拿 {@link #id()} 再查一次库(见 {@code AuthServiceImpl#currentUser})。
 * <p>
 * 做成 record 是因为它天生不可变 —— 这个对象会被放进 ThreadLocal 在整条请求链上传递,
 * 可变对象容易在某个环节被意外改掉, 排查起来很痛苦。
 */
public record CurrentUser(Long id, String username) {
}
