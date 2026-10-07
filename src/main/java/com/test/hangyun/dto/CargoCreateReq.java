package com.test.hangyun.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增订单货物请求。
 * <p>
 * ⚠️ **三个字段全都必填** —— 对应库里那三列都是 `not null`(见 initial.sql)。
 * 少了任何一个, 数据库会直接拒绝插入, 所以在这里就挡住, 返回 400 而不是让它变成 500。
 */
@Data
public class CargoCreateReq {

    /** 所属订单号, 必填, 必须是 orders 中已存在的订单 */
    @NotBlank(message = "订单号不能为空")
    @Size(max = 50, message = "订单号长度不能超过 50")
    private String orderId;

    /** 货物种类ID, 必填, 必须是 cargo_type 中已存在的记录 */
    @NotNull(message = "货物种类不能为空")
    private Long cargoTypeId;

    /** 数量, 必填 */
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    private Integer quantity;
}
