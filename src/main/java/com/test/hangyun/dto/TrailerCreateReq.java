package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增拖车请求。
 * <p>
 * 拖车号是业务主键, 由**前端提供**(如 沪A12345), 后端不生成; 所以新增必填、且不可重复。
 * 修改时拖车号不能改 —— 见 {@link TrailerUpdateReq}, 那里没有这个字段。
 */
@Data
public class TrailerCreateReq {

    /** 拖车号, 必填, 最长 30 */
    @NotBlank(message = "拖车号不能为空")
    @Size(max = 30, message = "拖车号长度不能超过 30")
    private String no;

    /** 联系电话, 可空 */
    @Size(max = 30, message = "联系电话长度不能超过 30")
    private String phone;

    /** 司机姓名, 可空 */
    @Size(max = 50, message = "司机姓名长度不能超过 50")
    private String name;
}
