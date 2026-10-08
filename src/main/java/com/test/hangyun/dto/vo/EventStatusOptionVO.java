package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.EventStatus;
import lombok.Data;

/**
 * 集装箱事件状态下拉框候选项（给"新增物流事件"选状态用）。
 */
@Data
public class EventStatusOptionVO {

    /** 提交事件时要用这个 id（eventStatusId） */
    private Long id;

    /** 状态中文描述, 下拉框显示的主文本 */
    private String descriptionCn;

    /** 状态英文描述 */
    private String descriptionEn;

    public static EventStatusOptionVO from(EventStatus e) {
        if (e == null) {
            return null;
        }
        EventStatusOptionVO vo = new EventStatusOptionVO();
        vo.setId(e.getId());
        vo.setDescriptionCn(e.getDescriptionCn());
        vo.setDescriptionEn(e.getDescriptionEn());
        return vo;
    }
}
