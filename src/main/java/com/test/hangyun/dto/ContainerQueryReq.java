package com.test.hangyun.dto;

import lombok.Data;

/**
 * 集装箱列表查询条件。
 */
@Data
public class ContainerQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 箱号, 前缀匹配(如传 "SEGU" 命中所有 SEGU 开头的箱) */
    private String no;

    /** 集装箱状态ID, 精确匹配 */
    private Long status;
}
