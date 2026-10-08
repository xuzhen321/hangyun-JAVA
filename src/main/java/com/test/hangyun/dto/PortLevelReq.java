package com.test.hangyun.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 港口级别的新增/修改请求（整体覆盖）。字段没有唯一约束，不查重。 */
@Data
public class PortLevelReq {

    @NotNull(message = "港口级别不能为空")
    @Min(value = 0, message = "港口级别不能为负数")
    private Integer level;
}
