package com.test.hangyun.controller;

import com.test.hangyun.common.Result;
import com.test.hangyun.dto.LoginReq;
import com.test.hangyun.dto.vo.LoginVO;
import com.test.hangyun.dto.vo.UserVO;
import com.test.hangyun.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证。
 * <p>
 * ⚠️ {@code /auth/login} 是**免校验**的(见 WebMvcConfig 的白名单), 其余两个要求已登录。
 */
@Tag(name = "登录认证", description = "登录 / 登出 / 当前用户")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录(返回令牌和用户信息)")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.login(req));
    }

    @Operation(summary = "登出(无状态令牌, 前端丢弃本地令牌即可)")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(authService.currentUser());
    }
}
