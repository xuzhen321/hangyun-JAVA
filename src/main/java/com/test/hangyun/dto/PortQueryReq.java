package com.test.hangyun.dto;

import lombok.Data;

/**
 * 港口列表查询条件。
 */
@Data
public class PortQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /**
     * 关键字, **前缀匹配**: 同时匹配**五字码**、**中文名**、**英文名**, 任一命中即返回。
     */
    private String keyword;

    /** 国家ID, 精确匹配 */
    private Long countryId;
}
