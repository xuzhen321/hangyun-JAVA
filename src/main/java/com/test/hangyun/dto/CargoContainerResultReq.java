package com.test.hangyun.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 货物装箱结果的新增/修改请求。
 * <p>
 * 三个字段语义相同(整体覆盖), 所以新增和修改共用一个 DTO。
 * <p>
 * ⚠️ **三个字段全必填** —— 对应库里那三列都是 `not null`。
 * 少了任何一个都会返回 400, 不会静默存成空值。
 */
@Data
public class CargoContainerResultReq {

    /** 货物ID, 必填, 必须是 cargo 中已存在的记录 */
    @NotNull(message = "货物不能为空")
    private Long cargoId;

    /** 集装箱号, 必填, 最长 30, 且必须是 container 中**已存在**的箱号 */
    @NotBlank(message = "集装箱号不能为空")
    @Size(max = 30, message = "集装箱号长度不能超过 30")
    private String containerNo;

    /** 装入数量, 必填 */
    @NotNull(message = "装入数量不能为空")
    @Min(value = 0, message = "装入数量不能为负数")
    private Integer quantity;
}
