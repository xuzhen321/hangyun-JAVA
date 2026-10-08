package com.test.hangyun.dto;

import lombok.Data;

/**
 * 拖车列表查询条件。
 */
@Data
public class TrailerQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /**
     * 关键字, **前缀匹配**: 同时匹配**拖车号**和**司机姓名**, 任一命中即返回。
     * 传 "沪A" 能命中该车牌的拖车, 传 "张" 能命中司机姓张的拖车。
     */
    private String keyword;
}
