package com.test.hangyun.dto;

import lombok.Data;

/** 港口类型列表查询条件。 */
@Data
public class PortTypeQueryReq {

    private Integer page = 1;

    private Integer size = 20;

    /** 类型，前缀匹配 */
    private String keyword;
}
