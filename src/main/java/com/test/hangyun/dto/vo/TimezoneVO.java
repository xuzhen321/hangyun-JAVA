package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Timezone;
import lombok.Data;

import java.time.LocalDateTime;

/** 时区响应对象。 */
@Data
public class TimezoneVO {

    private Long id;

    /** 港口时区 */
    private String timezoneUtc8;

    /** 港口时区(+8) */
    private String timezoneAsiaShanghai;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static TimezoneVO from(Timezone e) {
        if (e == null) {
            return null;
        }
        TimezoneVO vo = new TimezoneVO();
        vo.setId(e.getId());
        vo.setTimezoneUtc8(e.getTimezoneUtc8());
        vo.setTimezoneAsiaShanghai(e.getTimezoneAsiaShanghai());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
