package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Vessel;
import lombok.Data;

/**
 * 船舶下拉框候选项（给"新增航次"选船用）。
 * <p>
 * 船上没有唯一的"业务标识"(不像箱号、车牌), 所以带上 MMSI —— 它是船舶的全球唯一识别码,
 * 名字相近的船可以靠它区分。
 * <p>
 * ⚠️ 数据取自**基表 vessel**, 不是视图 v_vessel —— 下拉框只要船名/MMSI,
 * 不需要联表(这是项目里 /xxx/options 的统一约定, 见后端接口设计文档 5.2)。
 */
@Data
public class VesselOptionVO {

    /** 提交航次时要用这个 id（vslId） */
    private Long id;

    /** 船名, 下拉框显示的主文本 */
    private String shipname;

    /** MMSI, 用来消歧 */
    private String mmsi;

    public static VesselOptionVO from(Vessel v) {
        if (v == null) {
            return null;
        }
        VesselOptionVO vo = new VesselOptionVO();
        vo.setId(v.getId());
        vo.setShipname(v.getName());
        vo.setMmsi(v.getMmsi());
        return vo;
    }
}
