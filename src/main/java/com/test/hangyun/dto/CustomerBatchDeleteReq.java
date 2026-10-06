package com.test.hangyun.dto;

import com.test.hangyun.constant.CustomerConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除客户请求。
 * <p>
 * 语义是"宽松"的: 存在的客户会被注销, **不存在的 id 直接忽略**, 接口仍然返回成功。
 * 因此这个接口是幂等的 —— 重复提交同一批 id 不会报错。
 */
@Data
public class CustomerBatchDeleteReq {

    /** 客户ID列表, 不能为空 */
    @NotEmpty(message = "客户ID列表不能为空")
    @Size(max = CustomerConstants.MAX_BATCH_DELETE_SIZE,
            message = "一次最多删除 " + CustomerConstants.MAX_BATCH_DELETE_SIZE + " 个客户")
    private List<@NotNull(message = "客户ID不能为空") Long> ids;
}
