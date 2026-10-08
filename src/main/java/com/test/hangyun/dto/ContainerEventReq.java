package com.test.hangyun.dto;

import com.test.hangyun.pojo.enums.EstimateFlag;
import com.test.hangyun.pojo.enums.EventSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流事件的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * <p>
 * ⚠️ **两个标志位提交时也传中文文字**, 不要传 '1'/'0'、'Y'/'N':
 * <pre>
 *   source        : "船公司" 或 "港区"
 *   isEsti        : "实际" 或 "预计"   ← Y=预计发生, N=实际发生, 别按字面理解
 * </pre>
 * 传了别的值会返回 400。
 */
@Data
public class ContainerEventReq {

    /** 箱号, 必填, 必须是已存在的集装箱(且未删除) */
    @NotBlank(message = "箱号不能为空")
    @Size(max = 30, message = "箱号长度不能超过 30")
    private String containerNo;

    /** 数据来源: "船公司" / "港区" */
    private EventSource source;

    /** 航次ID, 必须是已存在的航次 */
    private Long voyageId;

    /** 发生地ID, 必须是已存在的港口 */
    private Long eventPlaceId;

    /** 事件状态ID, 必须是已存在的事件状态(用 /event-statuses/options 选) */
    private Long eventStatusId;

    /** "实际" / "预计" */
    private EstimateFlag isEsti;

    /** 状态发生时间 */
    private LocalDateTime eventTime;
}
