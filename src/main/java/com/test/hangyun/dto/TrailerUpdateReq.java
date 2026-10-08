package com.test.hangyun.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改拖车请求。语义是整体覆盖: 未传的字段会被置为 null。
 * <p>
 * ⚠️ **没有 no 字段** —— 拖车号是主键, 不可修改。要"换拖车号"只能删了重建。
 * 前端做编辑表单时, 拖车号那一栏应该是**只读的**。
 */
@Data
public class TrailerUpdateReq {

    /** 联系电话, 可空 */
    @Size(max = 30, message = "联系电话长度不能超过 30")
    private String phone;

    /** 司机姓名, 可空 */
    @Size(max = 50, message = "司机姓名长度不能超过 50")
    private String name;
}
