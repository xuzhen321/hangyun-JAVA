package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 集装箱事件状态的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * <p>
 * ⚠️ **中英文两列在库里都有唯一约束**, 所以两个都要查重。
 * 中文描述必填(页面展示用中文), 英文描述可空。
 */
@Data
public class EventStatusReq {

    /** 状态中文描述, 必填 */
    @NotBlank(message = "状态中文描述不能为空")
    @Size(max = 50, message = "状态中文描述长度不能超过 50")
    private String descriptionCn;

    /** 状态英文描述, 可空 */
    @Size(max = 50, message = "状态英文描述长度不能超过 50")
    private String descriptionEn;
}
