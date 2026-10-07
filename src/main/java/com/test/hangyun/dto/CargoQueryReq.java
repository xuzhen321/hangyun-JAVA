package com.test.hangyun.dto;

import lombok.Data;

/**
 * 订单货物列表查询条件。
 * <p>
 * 两个筛选条件都是可选的, 同时传就是 AND。
 */
@Data
public class CargoQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /**
     * 货物名称, **前缀匹配**。
     * 注意名称在 cargo_type 表上, cargo 里只有 cargo_type_id,
     * 所以这个条件是由 Service 先查出种类 id 再筛的(见 CargoServiceImpl#page)。
     */
    private String cargoTypeName;

    /** 订单号, **精确匹配**(订单号是标识符, 用户一般整串复制粘贴) */
    private String orderId;
}
