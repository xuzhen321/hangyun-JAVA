package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Area;
import lombok.Data;

import java.time.LocalDateTime;

/** 区域响应对象。 */
@Data
public class AreaVO {

    private Long id;
    private String areaName;
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static AreaVO from(Area e) {
        if (e == null) {
            return null;
        }
        AreaVO vo = new AreaVO();
        vo.setId(e.getId());
        vo.setAreaName(e.getAreaName());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
