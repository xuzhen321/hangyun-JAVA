package com.test.hangyun.service;

import com.test.hangyun.dto.LoginReq;
import com.test.hangyun.dto.vo.LoginVO;
import com.test.hangyun.dto.vo.UserVO;

/**
 * 登录认证。
 * <p>
 * 令牌是**无状态**的 JWT, 服务端不存会话。
 */
public interface AuthService {

    /** 登录。账号不存在/密码错/被冻结都会抛异常拒绝 */
    LoginVO login(LoginReq req);

    /** 当前登录用户(从令牌解析出的身份再查一次库, 拿最新的姓名/状态) */
    UserVO currentUser();

    /** 登出 */
    void logout();
}
