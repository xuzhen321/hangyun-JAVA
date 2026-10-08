package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Timezone;
import lombok.Data;

/** 时区下拉框候选项（给"新增港口"选时区用）。 */
@Data
public class TimezoneOptionVO {

    /** 提交港口时要用这个 id（timezoneId） */
    private Long id;

    private String timezoneUtc8;

    private String timezoneAsiaShanghai;

    public static TimezoneOptionVO from(Timezone e) {
        if (e == null) {
            return null;
        }
        TimezoneOptionVO vo = new TimezoneOptionVO();
        vo.setId(e.getId());
        vo.setTimezoneUtc8(e.getTimezoneUtc8());
        vo.setTimezoneAsiaShanghai(e.getTimezoneAsiaShanghai());
        return vo;
    }
}
