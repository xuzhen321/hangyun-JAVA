package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 集装箱箱型的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护。
 */
@Data
public class ContainerTypeReq {

    /** 箱型类别, 必填, 如普通箱 / 高箱 / 冷藏箱 */
    @NotBlank(message = "箱型类别不能为空")
    @Size(max = 50, message = "箱型类别长度不能超过 50")
    private String type;

    /** 箱尺寸, 可空, 如 20英尺 / 40英尺 */
    @Size(max = 20, message = "箱尺寸长度不能超过 20")
    private String size;
}
