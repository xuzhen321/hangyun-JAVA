package com.test.hangyun.dto;

import lombok.Data;

/**
 * 船舶类型列表查询条件。
 */
@Data
public class ShipTypeQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 类型名称, 前缀匹配 */
    private String keyword;
}
