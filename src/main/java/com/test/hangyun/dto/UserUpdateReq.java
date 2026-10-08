package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改用户请求。语义是整体覆盖: 未传的字段会被置为 null。
 * <p>
 * ⚠️ **密码不在这里** —— 改密码走单独的 {@code PUT /users/{id}/password}。
 * <p>
 * 原因是"整体覆盖"碰上密码会歧义: 编辑表单不填密码时, 到底是"不改密码"还是"把密码清空"?
 * 清空等于把账号锁死, 太危险。把密码从修改里摘出去, 两种语义就都不需要猜了。
 */
@Data
public class UserUpdateReq {

    /** 登录名, 必填, 库里有唯一约束 */
    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过 50")
    private String username;

    /** 真实姓名, 可空 */
    @Size(max = 100, message = "真实姓名长度不能超过 100")
    private String realName;

    /** 电话, 可空 */
    @Size(max = 30, message = "电话长度不能超过 30")
    private String phone;

    /** 账号状态: 0正常 / 1冻结 / 2已删除 */
    @Pattern(regexp = "[012]", message = "账号状态只能是 0正常 / 1冻结 / 2已删除")
    private String status;
}
