package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Company;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公司响应对象。
 */
@Data
public class CompanyVO {

    private Long id;

    /** 公司名称 */
    private String name;

    /** 公司代码(箱主代码) */
    private String code;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static CompanyVO from(Company e) {
        if (e == null) {
            return null;
        }
        CompanyVO vo = new CompanyVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setCode(e.getCode());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
