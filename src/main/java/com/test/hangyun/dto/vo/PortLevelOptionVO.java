package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.PortLevel;
import lombok.Data;

/** 港口级别下拉框候选项（给"新增港口"选级别用）。 */
@Data
public class PortLevelOptionVO {

    /** 提交港口时要用这个 id（levelId） */
    private Long id;

    private Integer level;

    public static PortLevelOptionVO from(PortLevel e) {
        if (e == null) {
            return null;
        }
        PortLevelOptionVO vo = new PortLevelOptionVO();
        vo.setId(e.getId());
        vo.setLevel(e.getLevel());
        return vo;
    }
}
