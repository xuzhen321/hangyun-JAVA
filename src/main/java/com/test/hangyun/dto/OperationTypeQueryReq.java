package com.test.hangyun.dto;

import lombok.Data;

/**
 * 操作类型列表查询条件。
 */
@Data
public class OperationTypeQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 操作类型名称, 前缀匹配(从开头匹配) */
    private String keyword;
}
