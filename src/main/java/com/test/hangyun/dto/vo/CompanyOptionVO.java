package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Company;
import lombok.Data;

/**
 * 公司下拉框候选项（给"新增集装箱"选箱主 / 操作方用）。
 * <p>
 * ⚠️ **必须同时带 name 和 code**：只给公司名, 用户分不清"中远海运"和"中远海运（香港）"这种
 * 名字相近的公司；代码是唯一的业务标识, 两个一起显示才能确认选对了。
 */
@Data
public class CompanyOptionVO {

    /** 提交集装箱时要用这个 id（ownerId / operatorId） */
    private Long id;

    /** 公司名称, 下拉框显示的主文本 */
    private String name;

    /** 公司代码(箱主代码), 用来消歧 */
    private String code;

    public static CompanyOptionVO from(Company e) {
        if (e == null) {
            return null;
        }
        CompanyOptionVO vo = new CompanyOptionVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setCode(e.getCode());
        return vo;
    }
}
