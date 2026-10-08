package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Container;
import lombok.Data;

/**
 * 集装箱下拉框候选项（给"新增装箱结果"选箱子用）。
 * <p>
 * 箱号本身就是用户看得懂的标识（箱体上印着），所以 id 就是箱号本身，不用再额外带数字 id。
 * 带上状态是为了让用户认得出这个箱子现在能不能用。
 */
@Data
public class ContainerOptionVO {

    /** 箱号，**提交装箱结果时填这个**（containerNo） */
    private String no;

    /** 当前状态 ID */
    private Long statusId;

    /** 状态中文描述, 如"在堆场" */
    private String statusDescription;

    public static ContainerOptionVO of(Container e, String statusDescription) {
        if (e == null) {
            return null;
        }
        ContainerOptionVO vo = new ContainerOptionVO();
        vo.setNo(e.getNo());
        vo.setStatusId(e.getStatusId());
        vo.setStatusDescription(statusDescription);
        return vo;
    }
}
