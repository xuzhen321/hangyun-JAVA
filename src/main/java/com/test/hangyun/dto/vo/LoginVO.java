package com.test.hangyun.dto.vo;

import lombok.Data;

/**
 * 登录成功响应。
 * <p>
 * 把用户信息一起带回来, 前端就不用登录后再补一次 {@code GET /auth/me}
 * (登录页跳主界面时那一瞬间正是最需要它的时候)。
 * <p>
 * ⚠️ 其中 {@link UserVO} **不含密码**, 这里也不含。
 */
@Data
public class LoginVO {

    /** 访问令牌。之后每个请求都要带 {@code Authorization: Bearer <token>} */
    private String token;

    /** 令牌有效期(分钟)。前端可据此提前提示"即将过期" */
    private long expireMinutes;

    /** 当前登录用户 */
    private UserVO user;
}
