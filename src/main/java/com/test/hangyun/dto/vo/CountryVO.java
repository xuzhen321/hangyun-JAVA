package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Country;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国家响应对象。
 */
@Data
public class CountryVO {

    private Long id;

    /** 国家代码 */
    private String countryCode;

    /** 国家中文名 */
    private String countryCnname;

    /** 国家英文名 */
    private String countryEnname;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static CountryVO from(Country e) {
        if (e == null) {
            return null;
        }
        CountryVO vo = new CountryVO();
        vo.setId(e.getId());
        vo.setCountryCode(e.getCountryCode());
        vo.setCountryCnname(e.getCountryCnname());
        vo.setCountryEnname(e.getCountryEnname());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
