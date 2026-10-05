package com.test.hangyun.dto;

import lombok.Data;

/**
 * 客户状态列表查询条件。
 * <p>
 * 状态是字典表, 分页查询只接受页码和条数两个参数, 不提供关键字等筛选;
 * 需要取全集请走 GET /customer-statuses/all。
 */
@Data
public class CustomerStatusQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;
}
