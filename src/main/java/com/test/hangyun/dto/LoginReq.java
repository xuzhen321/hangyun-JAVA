package com.test.hangyun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求。
 * <p>
 * ⚠️ {@code password} 传的是**明文** —— HTTPS 负责传输安全, 后端只负责把它和库里的
 * BCrypt 哈希做比对, 自己不留明文。
 */
@Data
public class LoginReq {

    /** 登录名 */
    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过 50")
    private String username;

    /**
     * 密码(明文)。
     * <p>
     * ⚠️ {@code WRITE_ONLY}: 能提交进来, 但不会被任何序列化带出去(日志、回显都不会)。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码长度不能超过 64")
    private String password;
}
