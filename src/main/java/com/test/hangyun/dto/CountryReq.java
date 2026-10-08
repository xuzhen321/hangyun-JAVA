package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 国家的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * country_code 在库里有唯一约束, 所以新增/修改时会查重。
 */
@Data
public class CountryReq {

    /** 国家代码, 必填, 如 CN。库里有唯一约束 */
    @NotBlank(message = "国家代码不能为空")
    @Size(max = 10, message = "国家代码长度不能超过 10")
    private String countryCode;

    /** 国家中文名, 可空 */
    @Size(max = 100, message = "国家中文名长度不能超过 100")
    private String countryCnname;

    /** 国家英文名, 可空 */
    @Size(max = 100, message = "国家英文名长度不能超过 100")
    private String countryEnname;
}
