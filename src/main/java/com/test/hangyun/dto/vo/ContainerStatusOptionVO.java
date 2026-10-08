package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ContainerStatus;
import lombok.Data;

/**
 * 集装箱状态下拉框候选项（给"新增集装箱"选状态用）。
 * <p>
 * 中英文都给 —— 页面默认显示中文, 但有些界面(比如给外方看)要用英文。
 */
@Data
public class ContainerStatusOptionVO {

    /** 提交集装箱时要用这个 id（statusId） */
    private Long id;

    /** 状态中文描述, 下拉框显示的主文本 */
    private String descriptionCn;

    /** 状态英文描述 */
    private String descriptionEn;

    public static ContainerStatusOptionVO from(ContainerStatus e) {
        if (e == null) {
            return null;
        }
        ContainerStatusOptionVO vo = new ContainerStatusOptionVO();
        vo.setId(e.getId());
        vo.setDescriptionCn(e.getDescriptionCn());
        vo.setDescriptionEn(e.getDescriptionEn());
        return vo;
    }
}
