package com.test.hangyun.dto;

import lombok.Data;

/**
 * 港口级别列表查询条件。
 * <p>
 * 唯一的业务字段是数字, 做前缀搜索没意义, 所以只支持分页。
 */
@Data
public class PortLevelQueryReq {

    private Integer page = 1;

    private Integer size = 20;
}
