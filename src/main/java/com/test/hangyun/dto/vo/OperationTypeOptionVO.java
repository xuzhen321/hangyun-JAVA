package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.OperationType;
import lombok.Data;

/** 操作类型下拉框候选项。 */
@Data
public class OperationTypeOptionVO {

    /** 提交时要用这个 id */
    private Long id;

    /** 类型名称, 下拉框显示的主文本 */
    private String type;

    public static OperationTypeOptionVO from(OperationType e) {
        if (e == null) {
            return null;
        }
        OperationTypeOptionVO vo = new OperationTypeOptionVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        return vo;
    }
}
