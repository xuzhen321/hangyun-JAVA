package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 港口尺寸的新增/修改请求（整体覆盖）。字段没有唯一约束，不查重。 */
@Data
public class HarborSizeReq {

    @NotBlank(message = "港口尺寸不能为空")
    @Size(max = 20, message = "港口尺寸长度不能超过 20")
    private String size;
}
