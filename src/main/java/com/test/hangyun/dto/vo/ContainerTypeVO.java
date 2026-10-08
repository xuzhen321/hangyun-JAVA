package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ContainerType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱箱型响应对象。
 */
@Data
public class ContainerTypeVO {

    private Long id;

    /** 箱型类别: 如普通箱 */
    private String type;

    /** 箱尺寸: 如40英尺 */
    private String size;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static ContainerTypeVO from(ContainerType e) {
        if (e == null) {
            return null;
        }
        ContainerTypeVO vo = new ContainerTypeVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setSize(e.getSize());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
