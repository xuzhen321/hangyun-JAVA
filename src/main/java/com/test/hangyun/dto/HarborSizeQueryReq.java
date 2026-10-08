package com.test.hangyun.dto;

import lombok.Data;

/** 港口尺寸列表查询条件。 */
@Data
public class HarborSizeQueryReq {

    private Integer page = 1;

    private Integer size = 20;

    /** 尺寸，前缀匹配 */
    private String keyword;
}
