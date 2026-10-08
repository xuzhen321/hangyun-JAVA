package com.test.hangyun.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 船舶类型的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * <p>
 * ⚠️ **吨位和长度/宽度都必填** —— 它们描述的是该型船的规格, 船舶列表里
 * 「总吨 / 净吨 / 载重吨 / 长度 / 宽度」五列就是从**船型**联出来的:
 * 船型不填, 挂这个船型的船那五列就全是空白。所以在船型这一层就要求填全。
 */
@Data
public class ShipTypeReq {

    /** 船舶类型名称, 必填, 如集装箱船 */
    @NotBlank(message = "船舶类型名称不能为空")
    @Size(max = 50, message = "船舶类型名称长度不能超过 50")
    private String type;

    /** 总吨, 必填, 对应 decimal(12,2): 最多 10 位整数 + 2 位小数 */
    @NotNull(message = "总吨不能为空")
    @Digits(integer = 10, fraction = 2, message = "总吨最多 10 位整数、2 位小数")
    @DecimalMin(value = "0", message = "总吨不能为负数")
    private BigDecimal gt;

    /** 长度, 必填, 对应 decimal(8,2): 最多 6 位整数 + 2 位小数 */
    @NotNull(message = "长度不能为空")
    @Digits(integer = 6, fraction = 2, message = "长度最多 6 位整数、2 位小数")
    @DecimalMin(value = "0", message = "长度不能为负数")
    private BigDecimal length;

    /** 宽度, 必填, 对应 decimal(8,2) */
    @NotNull(message = "宽度不能为空")
    @Digits(integer = 6, fraction = 2, message = "宽度最多 6 位整数、2 位小数")
    @DecimalMin(value = "0", message = "宽度不能为负数")
    private BigDecimal width;

    /** 净吨, 必填, 对应 decimal(12,2) */
    @NotNull(message = "净吨不能为空")
    @Digits(integer = 10, fraction = 2, message = "净吨最多 10 位整数、2 位小数")
    @DecimalMin(value = "0", message = "净吨不能为负数")
    private BigDecimal nt;

    /** 载重吨, 必填, 对应 decimal(12,2) */
    @NotNull(message = "载重吨不能为空")
    @Digits(integer = 10, fraction = 2, message = "载重吨最多 10 位整数、2 位小数")
    @DecimalMin(value = "0", message = "载重吨不能为负数")
    private BigDecimal dwt;
}
