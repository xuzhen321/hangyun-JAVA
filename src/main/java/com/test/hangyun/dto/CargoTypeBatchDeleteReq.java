package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除货物种类请求。
 * <p>
 * ⚠️ 语义和客户/订单的批量删除**不同**: 那两个是逻辑删除, 不存在"删不掉"的情况, 所以可以"宽松跳过";
 * 货物种类是**物理删除且有引用保护**, 所以这里用的是**严格**语义 ——
 * 只要有一个被货物引用, 整批都拒绝, 不会悄悄留下几条没删掉。
 * (不存在的 id 仍然忽略, 那是幂等, 不是部分失败。)
 */
@Data
public class CargoTypeBatchDeleteReq {

    /** 货物种类ID列表, 不能为空 */
    @NotEmpty(message = "货物种类ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 个货物种类")
    private List<@NotNull(message = "货物种类ID不能为空") Long> ids;
}
