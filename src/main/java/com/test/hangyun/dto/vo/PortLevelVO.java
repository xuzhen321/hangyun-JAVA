package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.PortLevel;
import lombok.Data;

import java.time.LocalDateTime;

/** 港口级别响应对象。 */
@Data
public class PortLevelVO {

    private Long id;
    private Integer level;
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static PortLevelVO from(PortLevel e) {
        if (e == null) {
            return null;
        }
        PortLevelVO vo = new PortLevelVO();
        vo.setId(e.getId());
        vo.setLevel(e.getLevel());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
