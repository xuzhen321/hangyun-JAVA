package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改操作类型的请求。
 * <p>
 * ⚠️ **只有修改, 没有新增** —— 这张表是固定的字典(见
 * {@link com.test.hangyun.pojo.enums.OpType}), 不支持增删,
 * 所以项目里其他资源那一对 CreateReq / UpdateReq 在这里只需要一个。
 * <p>
 * 改的是**展示文字**, 不影响日志里已经记下的历史记录 ——
 * {@code Log.type_id} 存的是 id, 视图按 id 去 JOIN 名字。
 */
@Data
public class OperationTypeReq {

    /** 操作类型名称, 必填, 如 INSERT。库里有唯一约束 */
    @NotBlank(message = "操作类型名称不能为空")
    @Size(max = 50, message = "操作类型名称长度不能超过 50")
    private String type;
}
