package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除订单请求。
 * <p>
 * 注意 id 是**订单号(字符串)**, 不是数字 —— orders.id 是 varchar(50)。
 * <p>
 * 语义和客户那边一致, 是"宽松"的: 存在的订单会被取消, **不存在的订单号直接忽略**,
 * 接口仍然返回成功。因此这个接口是幂等的。
 */
@Data
public class OrderBatchDeleteReq {

    /** 订单号列表, 不能为空 */
    @NotEmpty(message = "订单号列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 个订单")
    private List<@NotBlank(message = "订单号不能为空") String> ids;
}
