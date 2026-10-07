package com.test.hangyun.dto;

import lombok.Data;

/**
 * 货物种类列表查询条件。
 */
@Data
public class CargoTypeQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 货物名称, 前缀匹配(从开头匹配, 走 name 上的 varchar_pattern_ops 索引) */
    private String name;
}
