package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Area;
import lombok.Data;

/** 区域下拉框候选项（给"新增港口"选区域用）。 */
@Data
public class AreaOptionVO {

    /** 提交港口时要用这个 id（areaId） */
    private Long id;

    private String areaName;

    public static AreaOptionVO from(Area e) {
        if (e == null) {
            return null;
        }
        AreaOptionVO vo = new AreaOptionVO();
        vo.setId(e.getId());
        vo.setAreaName(e.getAreaName());
        return vo;
    }
}
