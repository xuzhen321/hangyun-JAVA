package com.test.hangyun.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改客户请求。语义是整体覆盖: 未传的字段会被置为 null。
 * 注意: 不包含 insert_time / update_time —— 由数据库触发器维护, 后端不传。
 */
@Data
public class CustomerUpdateReq {

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 50, message = "客户名称长度不能超过 50")
    private String name;

    @Size(max = 30, message = "联系方式长度不能超过 30")
    private String phone;

    @Email(message = "电子邮箱格式不正确")
    @Size(max = 50, message = "电子邮箱长度不能超过 50")
    private String email;

    @Size(max = 255, message = "详细地址长度不能超过 255")
    private String address;

    @Size(max = 100, message = "资质信息长度不能超过 100")
    private String qualification;

    private LocalDateTime qualificationValidTo;

    private Long statusId;
}
