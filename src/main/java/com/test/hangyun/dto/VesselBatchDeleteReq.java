package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除船舶请求。
 * <p>
 * 船舶是**物理删除且有引用保护**(航次会引用它), 所以是**严格**语义:
 * 只要有一艘被航次引用, 整批都拒绝。不存在的 id 仍然忽略(幂等)。
 */
@Data
public class VesselBatchDeleteReq {

    /** 船舶ID列表, 不能为空 */
    @NotEmpty(message = "船舶ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 艘船")
    private List<@NotNull(message = "船舶ID不能为空") Long> ids;
}
