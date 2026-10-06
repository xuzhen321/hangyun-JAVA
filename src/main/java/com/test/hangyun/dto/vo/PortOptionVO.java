package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Port;
import lombok.Data;

/**
 * 港口下拉框候选项。
 * <p>
 * 四个字段都是给"认得出是哪个港"用的: 五字码最精确, 中英文名便于人读。
 * 提交订单时用 id。
 */
@Data
public class PortOptionVO {

    /** 提交订单时要用这个 id */
    private Long id;

    /** 港口五字码(UN/LOCODE), 如 CNSHA */
    private String unlocode;

    /** 港口中文名 */
    private String cnname;

    /** 港口英文名 */
    private String enname;

    public static PortOptionVO from(Port e) {
        if (e == null) {
            return null;
        }
        PortOptionVO vo = new PortOptionVO();
        vo.setId(e.getId());
        vo.setUnlocode(e.getUnlocode());
        vo.setCnname(e.getCnname());
        vo.setEnname(e.getEnname());
        return vo;
    }
}
