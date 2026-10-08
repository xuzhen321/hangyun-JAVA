package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 区域的新增/修改请求（整体覆盖）。area_name 有唯一约束，会查重。 */
@Data
public class AreaReq {

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 50, message = "区域名称长度不能超过 50")
    private String areaName;
}
