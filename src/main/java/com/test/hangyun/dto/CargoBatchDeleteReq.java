package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除订单货物请求。
 * <p>
 * 货物是**物理删除**, 且有引用保护(装箱结果表引用它), 所以这里是**严格**语义:
 * 只要有一条被引用, 整批都拒绝, 不做部分删除。
 * (不存在的 id 仍然忽略, 那是幂等, 不是部分失败。)
 */
@Data
public class CargoBatchDeleteReq {

    /** 货物ID列表, 不能为空 */
    @NotEmpty(message = "货物ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 条货物")
    private List<@NotNull(message = "货物ID不能为空") Long> ids;
}
