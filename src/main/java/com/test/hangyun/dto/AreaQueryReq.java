package com.test.hangyun.dto;

import lombok.Data;

/** 区域列表查询条件。 */
@Data
public class AreaQueryReq {

    private Integer page = 1;

    private Integer size = 20;

    /** 区域名称，前缀匹配 */
    private String keyword;
}
