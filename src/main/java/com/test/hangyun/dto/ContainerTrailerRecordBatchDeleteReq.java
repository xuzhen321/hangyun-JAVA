package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除提空箱记录请求。
 * <p>
 * 这张表是链路末端(没有别的表引用它), 物理删除即可, 所以是**宽松**语义:
 * 存在的删掉、不存在的忽略, 幂等。
 */
@Data
public class ContainerTrailerRecordBatchDeleteReq {

    /** 记录ID列表, 不能为空 */
    @NotEmpty(message = "记录ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 条记录")
    private List<@NotNull(message = "记录ID不能为空") Long> ids;
}
