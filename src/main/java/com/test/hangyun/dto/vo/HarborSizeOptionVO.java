package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.HarborSize;
import lombok.Data;

/** 港口尺寸下拉框候选项（给"新增港口"选尺寸用）。 */
@Data
public class HarborSizeOptionVO {

    /** 提交港口时要用这个 id（harborSizeId） */
    private Long id;

    private String size;

    public static HarborSizeOptionVO from(HarborSize e) {
        if (e == null) {
            return null;
        }
        HarborSizeOptionVO vo = new HarborSizeOptionVO();
        vo.setId(e.getId());
        vo.setSize(e.getSize());
        return vo;
    }
}
