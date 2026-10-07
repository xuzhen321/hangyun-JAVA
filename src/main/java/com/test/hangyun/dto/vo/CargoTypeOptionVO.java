package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.CargoType;
import lombok.Data;

/**
 * 货物种类下拉框候选项。
 * <p>
 * 只带 id + name: 下拉框只需要"显示什么、提交什么", 重量体积那些在这个场景用不上。
 */
@Data
public class CargoTypeOptionVO {

    /** 提交货物时要用这个 id */
    private Long id;

    /** 货物名称, 下拉框显示的主文本 */
    private String name;

    public static CargoTypeOptionVO from(CargoType e) {
        if (e == null) {
            return null;
        }
        CargoTypeOptionVO vo = new CargoTypeOptionVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        return vo;
    }
}
