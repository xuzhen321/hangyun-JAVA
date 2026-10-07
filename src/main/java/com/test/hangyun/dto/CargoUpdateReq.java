package com.test.hangyun.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改订单货物请求。
 * <p>
 * ⚠️ **没有 orderId 字段** —— 货物挂在哪个订单下是**不可改**的。
 * 这不只是"接口没有这个参数", Service 层的 UPDATE 语句也**结构上不会碰 order_id**,
 * 所以就算有人绕过校验也改不动。
 * <p>
 * 要换订单的话: 删掉这条货物, 再到目标订单下重新加一条。
 * <p>
 * ⚠️ **两个字段都必填** —— 对应那两列在库里是 `not null`。所以修改是"整体覆盖"语义,
 * 没传就是 400, 不会像别的模块那样把字段清空。
 */
@Data
public class CargoUpdateReq {

    /** 货物种类ID, 必填, 必须是 cargo_type 中已存在的记录 */
    @NotNull(message = "货物种类不能为空")
    private Long cargoTypeId;

    /** 数量, 必填 */
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    private Integer quantity;
}
