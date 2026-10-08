package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除货物装箱结果请求。
 * <p>
 * 装箱结果是**物理删除**。这条记录是"货物装进集装箱"的最终结果, 没有被别的表引用,
 * 所以不需要引用检查 —— 但**删掉就真的没了**, 前端删除前最好加确认。
 */
@Data
public class CargoContainerResultBatchDeleteReq {

    /** 记录ID列表, 不能为空 */
    @NotEmpty(message = "记录ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 条记录")
    private List<@NotNull(message = "记录ID不能为空") Long> ids;
}
