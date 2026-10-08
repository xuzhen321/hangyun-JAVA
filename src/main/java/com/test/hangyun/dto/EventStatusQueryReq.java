package com.test.hangyun.dto;

import lombok.Data;

/**
 * 集装箱事件状态列表查询条件。
 * <p>
 * 字典表, 分页只接受页码和条数; 取全集给下拉框用请走 /event-statuses/options。
 */
@Data
public class EventStatusQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;
}
