package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Trailer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拖车响应对象。
 */
@Data
public class TrailerVO {

    /** 拖车号(主键) */
    private String no;

    /** 联系电话 */
    private String phone;

    /** 司机姓名 */
    private String name;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static TrailerVO from(Trailer e) {
        if (e == null) {
            return null;
        }
        TrailerVO vo = new TrailerVO();
        vo.setNo(e.getNo());
        vo.setPhone(e.getPhone());
        vo.setName(e.getName());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
