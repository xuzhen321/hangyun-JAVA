package com.test.hangyun.dto;

import lombok.Data;

/** 时区列表查询条件。 */
@Data
public class TimezoneQueryReq {

    private Integer page = 1;

    private Integer size = 20;

    /** 关键字，前缀匹配两个时区字段中的任一个 */
    private String keyword;
}
