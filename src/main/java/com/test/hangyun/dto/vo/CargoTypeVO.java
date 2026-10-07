package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.CargoType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物种类响应对象。
 * 与实体分开, 保证接口契约不随表结构变化而波动。
 */
@Data
public class CargoTypeVO {

    private Long id;

    /** 货物名称 */
    private String name;

    /** 货物描述 */
    private String description;

    /** 单件重量(kg) */
    private BigDecimal weightKg;

    /** 单件体积(m³) */
    private BigDecimal volumeM3;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static CargoTypeVO from(CargoType e) {
        if (e == null) {
            return null;
        }
        CargoTypeVO vo = new CargoTypeVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setDescription(e.getDescription());
        vo.setWeightKg(e.getWeightKg());
        vo.setVolumeM3(e.getVolumeM3());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
