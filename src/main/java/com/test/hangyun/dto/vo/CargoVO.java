package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Cargo;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单货物响应对象。
 * <p>
 * cargoTypeName 不是 cargo 表里的列 —— 库里没有 v_cargo 视图, 这个字段由 Service 层
 * 二次查询 cargo_type 后填进来。
 */
@Data
public class CargoVO {

    /** 货物ID。自增数字, 不像订单号那样有精度问题, 可以直接当数字用 */
    private Long id;

    /** 货物种类ID */
    private Long cargoTypeId;

    /** 货物种类名称(由 Service 组装; 种类不存在或被清空时为 null) */
    private String cargoTypeName;

    /** 所属订单号 */
    private String orderId;

    /** 数量 */
    private Integer quantity;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    /** name 由调用方查好传进来, 因为 cargo 表里没有这一列 */
    public static CargoVO from(Cargo e, String cargoTypeName) {
        if (e == null) {
            return null;
        }
        CargoVO vo = new CargoVO();
        vo.setId(e.getId());
        vo.setCargoTypeId(e.getCargoTypeId());
        vo.setCargoTypeName(cargoTypeName);
        vo.setOrderId(e.getOrderId());
        vo.setQuantity(e.getQuantity());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
