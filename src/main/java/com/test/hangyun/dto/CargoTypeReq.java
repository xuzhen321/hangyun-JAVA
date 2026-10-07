package com.test.hangyun.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 货物种类的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护, 后端不传。
 */
@Data
public class CargoTypeReq {

    @NotBlank(message = "货物名称不能为空")
    @Size(max = 100, message = "货物名称长度不能超过 100")
    private String name;

    @Size(max = 255, message = "货物描述长度不能超过 255")
    private String description;

    /** 单件重量(kg), 对应 decimal(10,2): 最多 8 位整数 + 2 位小数 */
    @Digits(integer = 8, fraction = 2, message = "重量最多 8 位整数、2 位小数")
    @DecimalMin(value = "0", message = "重量不能为负数")
    private BigDecimal weightKg;

    /** 单件体积(m³), 对应 decimal(10,2): 最多 8 位整数 + 2 位小数 */
    @Digits(integer = 8, fraction = 2, message = "体积最多 8 位整数、2 位小数")
    @DecimalMin(value = "0", message = "体积不能为负数")
    private BigDecimal volumeM3;
}
