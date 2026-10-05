package com.test.hangyun.dto;

import lombok.Data;

/**
 * 客户列表查询条件。
 */
@Data
public class CustomerQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 关键字: 匹配客户名称或联系方式 */
    private String keyword;

    /** 客户状态ID, 精确匹配 */
    private Long status;
}
