package com.test.hangyun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增用户请求。
 * <p>
 * ⚠️ {@code password} 传的是**明文**, 后端用 BCrypt 哈希之后再落库 ——
 * 库里存的永远是哈希, 明文不落盘、不进日志。
 * <p>
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护。
 */
@Data
public class UserCreateReq {

    /** 登录名, 必填, 库里有唯一约束 */
    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过 50")
    private String username;

    /**
     * 明文密码, 必填。下限 6 位是防呆, 不是安全强度保证。
     * <p>
     * ⚠️ {@code WRITE_ONLY} 是必须的: 日志切面会把方法入参序列化成 JSON 存进
     * {@code Log.after_value}, 不加这个注解**密码明文会被原样写进操作日志**。
     * 加上之后: 能提交进来(反序列化), 但任何序列化(含日志)都不会带出去。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6~64 之间")
    private String password;

    /** 真实姓名, 可空 */
    @Size(max = 100, message = "真实姓名长度不能超过 100")
    private String realName;

    /** 电话, 可空 */
    @Size(max = 30, message = "电话长度不能超过 30")
    private String phone;

    /**
     * 账号状态, 可空(不传按 '0' 正常处理)。
     * <p>
     * 取值 0正常 / 1冻结 / 2已删除 —— 和客户状态一样, 这几个状态都是业务上真实存在的,
     * 所以允许直接传(删除接口只是把它置成 '2' 的一个便捷入口)。
     */
    @Pattern(regexp = "[012]", message = "账号状态只能是 0正常 / 1冻结 / 2已删除")
    private String status;
}
