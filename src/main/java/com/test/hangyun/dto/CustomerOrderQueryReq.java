package com.test.hangyun.dto;

import lombok.Data;

/**
 * 「某个客户名下的订单」查询条件。
 * <p>
 * 刻意只有分页两个参数: 客户在路径里, 不需要再传一次; 也不要状态、日期之类的筛选
 * —— 需求就是"这个客户的全部订单"。多一个字段就意味着多一个别人会误以为能用的参数。
 */
@Data
public class CustomerOrderQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;
}
