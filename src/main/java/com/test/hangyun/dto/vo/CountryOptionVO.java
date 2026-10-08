package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Country;
import lombok.Data;

/**
 * 国家下拉框候选项（给"新增船舶"选船旗国用）。
 * <p>
 * 中文名 + 代码都给: 中文名给人看, 代码用来消歧（有些国家中译名接近）。
 */
@Data
public class CountryOptionVO {

    /** 提交船舶时要用这个 id（countryId） */
    private Long id;

    /** 国家中文名, 下拉框显示的主文本 */
    private String countryCnname;

    /** 国家代码, 如 CN */
    private String countryCode;

    public static CountryOptionVO from(Country e) {
        if (e == null) {
            return null;
        }
        CountryOptionVO vo = new CountryOptionVO();
        vo.setId(e.getId());
        vo.setCountryCnname(e.getCountryCnname());
        vo.setCountryCode(e.getCountryCode());
        return vo;
    }
}
