package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.PortType;
import lombok.Data;

/** 港口类型下拉框候选项（给"新增港口"选类型用）。 */
@Data
public class PortTypeOptionVO {

    /** 提交港口时要用这个 id（portTypeId） */
    private Long id;

    private String type;

    public static PortTypeOptionVO from(PortType e) {
        if (e == null) {
            return null;
        }
        PortTypeOptionVO vo = new PortTypeOptionVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        return vo;
    }
}
