package com.test.hangyun.dto;

import lombok.Data;

/**
 * 航次列表查询条件。
 */
@Data
public class VoyageQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 航次号, 前缀匹配 */
    private String no;

    /** 起始港口ID, 精确匹配 */
    private Long loadingPortId;
}
