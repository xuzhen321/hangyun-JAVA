package com.test.hangyun.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单列表查询条件。
 * <p>
 * 每个条件都是可选的, 不传即不参与筛选, 传了才 AND 上去。
 * 按客户筛选用的是**姓名前缀**而不是 id —— 列表筛选是给人用的, 没人记得住 id。
 */
@Data
public class OrderQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 客户姓名, 前缀匹配(从开头匹配) */
    private String customerName;

    /** 订单状态ID, 精确匹配 */
    private Long status;

    /** 下单时间下界(含), 与 orderDateTo 组成闭区间 */
    private LocalDateTime orderDateFrom;

    /** 下单时间上界(含), 与 orderDateFrom 组成闭区间 */
    private LocalDateTime orderDateTo;
}
