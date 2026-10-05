package com.test.hangyun.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户列表查询条件。
 * <p>
 * 每个条件都是可选的, 不传即不参与筛选, 传了才 AND 上去。
 * 名称和资质走前缀匹配(见 CustomerServiceImpl#page 的说明), 有效期走闭区间。
 */
@Data
public class CustomerQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 客户名称, 前缀匹配(从开头匹配) */
    private String name;

    /** 资质信息, 前缀匹配(从开头匹配) */
    private String qualification;

    /** 资质有效期下界(含), 与 qualificationValidToTo 组成闭区间 */
    private LocalDateTime qualificationValidToFrom;

    /** 资质有效期上界(含), 与 qualificationValidToFrom 组成闭区间 */
    private LocalDateTime qualificationValidToTo;

    /** 客户状态ID, 精确匹配 */
    private Long status;
}
