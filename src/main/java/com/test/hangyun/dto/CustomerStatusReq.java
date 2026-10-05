package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户状态的新增/修改请求。
 * <p>
 * 只有一个字段, 且新增与修改的语义相同(整体覆盖), 所以两个操作共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护, 后端不传。
 */
@Data
public class CustomerStatusReq {

    @NotBlank(message = "状态描述不能为空")
    @Size(max = 50, message = "状态描述长度不能超过 50")
    private String description;
}
