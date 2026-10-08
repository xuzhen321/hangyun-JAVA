package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ContainerStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱状态响应对象。
 */
@Data
public class ContainerStatusVO {

    private Long id;

    /** 状态中文描述 */
    private String descriptionCn;

    /** 状态英文描述 */
    private String descriptionEn;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static ContainerStatusVO from(ContainerStatus e) {
        if (e == null) {
            return null;
        }
        ContainerStatusVO vo = new ContainerStatusVO();
        vo.setId(e.getId());
        vo.setDescriptionCn(e.getDescriptionCn());
        vo.setDescriptionEn(e.getDescriptionEn());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
