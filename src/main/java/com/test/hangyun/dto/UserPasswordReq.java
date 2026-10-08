package com.test.hangyun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求（{@code PUT /users/{id}/password}）。
 * <p>
 * 语义是**直接覆盖**成新密码 —— 和"修改用户"分开成两个接口的原因见 {@link UserUpdateReq}。
 * <p>
 * 📌 多用户项目里更常见的做法是要求"先输旧密码", 但那属于"用户自助改密码"的场景;
 * 这里的接口是给**管理员重置**用的, 所以不要求旧密码。
 */
@Data
public class UserPasswordReq {

    /**
     * 新密码(明文), 后端用 BCrypt 哈希后落库。
     * <p>
     * ⚠️ {@code WRITE_ONLY} 让它可以提交进来, 但**不会被任何序列化带出去** ——
     * 否则日志切面会把新密码明文写进 {@code Log.after_value}。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6~64 之间")
    private String newPassword;
}
