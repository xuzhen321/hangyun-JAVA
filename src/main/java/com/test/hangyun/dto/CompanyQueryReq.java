package com.test.hangyun.dto;

import lombok.Data;

/**
 * 公司列表查询条件。
 */
@Data
public class CompanyQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /**
     * 关键字, **前缀匹配**: 同时匹配**公司名称**和**公司代码**, 任一命中即返回。
     * 传 "COS" 能命中代码为 COSU 的公司, 传 "中远" 能命中名称以"中远"开头的公司。
     */
    private String keyword;
}
