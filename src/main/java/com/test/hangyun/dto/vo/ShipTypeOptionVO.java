package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ShipType;
import lombok.Data;

/**
 * 船舶类型下拉框候选项（给"新增船舶"选船型用）。
 * <p>
 * 带上总吨(GT): 船型名字相近时(比如都是"集装箱船"), 吨位能帮着区分大小。
 */
@Data
public class ShipTypeOptionVO {

    /** 提交船舶时要用这个 id（vesselTypeId） */
    private Long id;

    /** 船舶类型名称, 下拉框显示的主文本 */
    private String type;

    /** 总吨 */
    private java.math.BigDecimal gt;

    public static ShipTypeOptionVO from(ShipType e) {
        if (e == null) {
            return null;
        }
        ShipTypeOptionVO vo = new ShipTypeOptionVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setGt(e.getGt());
        return vo;
    }
}
