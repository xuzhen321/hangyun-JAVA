package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 时区的新增/修改请求（整体覆盖）。
 * <p>
 * ⚠️ 接口字段叫 {@code timezoneUtc8} / {@code timezoneAsiaShanghai}, 对应库里那两个
 * 带引号的列名 {@code "timezone_UTC8"} / {@code "timezone_Asia_Shanghai"}。
 * <p>
 * 两列都有唯一约束，所以两个都会查重；两个都必填（一条时区记录缺一个就说不清）。
 */
@Data
public class TimezoneReq {

    /** 港口时区, 必填 */
    @NotBlank(message = "港口时区不能为空")
    @Size(max = 20, message = "港口时区长度不能超过 20")
    private String timezoneUtc8;

    /** 港口时区(+8), 必填 */
    @NotBlank(message = "港口时区(+8)不能为空")
    @Size(max = 50, message = "港口时区(+8)长度不能超过 50")
    private String timezoneAsiaShanghai;
}
