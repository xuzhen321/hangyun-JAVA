package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.HarborSize;
import lombok.Data;

import java.time.LocalDateTime;

/** 港口尺寸响应对象。 */
@Data
public class HarborSizeVO {

    private Long id;
    private String size;
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static HarborSizeVO from(HarborSize e) {
        if (e == null) {
            return null;
        }
        HarborSizeVO vo = new HarborSizeVO();
        vo.setId(e.getId());
        vo.setSize(e.getSize());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
