package com.test.hangyun.dto;

import lombok.Data;

/**
 * 货物装箱结果列表查询条件。
 * <p>
 * 三个筛选条件都可选, 同时传就是 AND。
 */
@Data
public class CargoContainerResultQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /**
     * 货物名称, **前缀匹配**。
     * 名称在 cargo_type 表上, 和装箱结果隔了两层(cargo -> cargo_type),
     * 所以这个条件由 Service 分步查出来再筛(见 CargoContainerResultServiceImpl#page)。
     */
    private String cargoTypeName;

    /**
     * 订单号, **精确匹配**。
     * 订单号也不在本表上(cargo_container_result -> cargo -> order_id),
     * 但它和上面的货物名称是**同一次 cargo 查询**里一起筛的, 不多花开销。
     */
    private String orderId;

    /** 集装箱号, **精确匹配**(箱号是标识符, 用户一般整串复制粘贴) */
    private String containerNo;
}
