package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.EventStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱事件状态响应对象。
 */
@Data
public class EventStatusVO {

    private Long id;

    /** 状态中文描述 */
    private String descriptionCn;

    /** 状态英文描述 */
    private String descriptionEn;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static EventStatusVO from(EventStatus e) {
        if (e == null) {
            return null;
        }
        EventStatusVO vo = new EventStatusVO();
        vo.setId(e.getId());
        vo.setDescriptionCn(e.getDescriptionCn());
        vo.setDescriptionEn(e.getDescriptionEn());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
