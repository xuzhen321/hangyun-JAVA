package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Cargo;
import lombok.Data;

/**
 * 货物下拉框候选项（给"新增装箱结果"选货物用）。
 * <p>
 * 为什么带 orderId: **同一批货物名可能在多个订单里都有**（比如两张订单都有"钢材"），
 * 只显示名称的话用户分不出该选哪一条。带上订单号才能对上。
 */
@Data
public class CargoOptionVO {

    /** 提交装箱结果时要用这个 id（cargoId） */
    private Long id;

    /** 货物名称（即货物种类的名称），下拉框显示的主文本 */
    private String cargoTypeName;

    /** 所属订单号，用来区分同名货物 */
    private String orderId;

    /** 数量 */
    private Integer quantity;

    public static CargoOptionVO of(Cargo e, String cargoTypeName) {
        if (e == null) {
            return null;
        }
        CargoOptionVO vo = new CargoOptionVO();
        vo.setId(e.getId());
        vo.setCargoTypeName(cargoTypeName);
        vo.setOrderId(e.getOrderId());
        vo.setQuantity(e.getQuantity());
        return vo;
    }
}
