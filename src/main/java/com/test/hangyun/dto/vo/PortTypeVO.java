package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.PortType;
import lombok.Data;

import java.time.LocalDateTime;

/** 港口类型响应对象。 */
@Data
public class PortTypeVO {

    private Long id;
    private String type;
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static PortTypeVO from(PortType e) {
        if (e == null) {
            return null;
        }
        PortTypeVO vo = new PortTypeVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
