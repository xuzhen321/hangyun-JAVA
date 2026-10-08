package com.test.hangyun.dto;

import com.test.hangyun.constant.BatchConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除(逻辑删除)用户请求。
 * <p>
 * 语义是"宽松"的: 存在的用户会被标记删除, **不存在的 id 直接忽略**, 接口仍然返回成功。
 * 因此这个接口是幂等的 —— 重复提交同一批 id 不会报错。
 * <p>
 * ⚠️ 内置管理员(id=1)不允许删除, 出现在批次里会**整批拒绝**(见 UserServiceImpl)。
 */
@Data
public class UserBatchDeleteReq {

    /** 用户ID列表, 不能为空 */
    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = BatchConstants.MAX_DELETE_SIZE,
            message = "一次最多删除 " + BatchConstants.MAX_DELETE_SIZE + " 个用户")
    private List<@NotNull(message = "用户ID不能为空") Long> ids;
}
