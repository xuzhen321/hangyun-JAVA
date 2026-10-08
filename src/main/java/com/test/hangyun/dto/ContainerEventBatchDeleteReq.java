package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除物流事件请求（逻辑删除）。
 * <p>
 * 和客户/订单/集装箱一样是**宽松**语义: 存在的改成"已删除"、不存在的忽略, 幂等。
 * (行还在, 没有"删不掉"的情况, 所以不需要整批拒绝。)
 */
@Data
public class ContainerEventBatchDeleteReq {

    /** 事件ID列表, 不能为空 */
    @NotEmpty(message = "事件ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 条事件")
    private List<@NotNull(message = "事件ID不能为空") Long> ids;
}
