package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公司的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护。
 * <p>
 * ⚠️ `name` 和 `code` 在库里**都没有唯一约束**, 所以允许重名、也允许代码重复, 新增时不查重。
 */
@Data
public class CompanyReq {

    /** 公司名称, 必填 */
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 100, message = "公司名称长度不能超过 100")
    private String name;

    /** 公司代码(箱主代码, BIC 四字码), 可空 */
    @Size(max = 20, message = "公司代码长度不能超过 20")
    private String code;
}
