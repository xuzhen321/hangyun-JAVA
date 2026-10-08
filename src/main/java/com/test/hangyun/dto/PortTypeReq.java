package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 港口类型的新增/修改请求（整体覆盖）。字段没有唯一约束，不查重。 */
@Data
public class PortTypeReq {

    /** 港口类型: 1系统规范港口, 2用户自定义港口 */
    @NotBlank(message = "港口类型不能为空")
    @Size(max = 20, message = "港口类型长度不能超过 20")
    private String type;
}
