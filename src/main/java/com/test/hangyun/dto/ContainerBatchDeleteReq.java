package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除集装箱请求。
 * <p>
 * 注意元素是**箱号(字符串)**, 不是数字 —— container 的主键是 varchar(30)。
 * <p>
 * 集装箱是**物理删除且有引用保护**(装箱结果会引用箱号), 所以这里是**严格**语义:
 * 只要有一个被引用, 整批都拒绝。不存在的箱号仍然忽略(幂等)。
 */
@Data
public class ContainerBatchDeleteReq {

    /** 箱号列表, 不能为空 */
    @NotEmpty(message = "箱号列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 个集装箱")
    private List<@NotBlank(message = "箱号不能为空") String> nos;
}
