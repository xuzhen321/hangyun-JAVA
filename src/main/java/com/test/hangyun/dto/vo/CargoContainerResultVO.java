package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.CargoContainerResult;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货物装箱结果响应对象。
 * <p>
 * cargoTypeName 和 orderId 都**不是本表的列**: 库里没有对应视图, 这两个字段由 Service 层
 * 沿 cargo_container_result -> cargo 查出来, 名称再往上一跳(cargo -> cargo_type)拿到。
 * 两条信息走的是同一次 cargo 查询, 不多花开销。
 */
@Data
public class CargoContainerResultVO {

    /** 记录ID。自增数字, 没有精度问题, 可以直接当数字用 */
    private Long id;

    /** 货物ID */
    private Long cargoId;

    /** 货物名称(即货物种类的名称; 由 Service 组装) */
    private String cargoTypeName;

    /** 所属订单号(由 Service 组装; 是字符串, 注意 7.2 的精度说明) */
    private String orderId;

    /** 集装箱号 */
    private String containerNo;

    /** 装入数量 */
    private Integer quantity;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    /** cargoTypeName / orderId 由调用方查好传进来, 因为都跨表 */
    public static CargoContainerResultVO from(CargoContainerResult e,
                                              String cargoTypeName, String orderId) {
        if (e == null) {
            return null;
        }
        CargoContainerResultVO vo = new CargoContainerResultVO();
        vo.setId(e.getId());
        vo.setCargoId(e.getCargoId());
        vo.setCargoTypeName(cargoTypeName);
        vo.setOrderId(orderId);
        vo.setContainerNo(e.getContainerNo());
        vo.setQuantity(e.getQuantity());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
