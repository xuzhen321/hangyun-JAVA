package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提空箱登记的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护。
 */
@Data
public class ContainerTrailerRecordReq {

    /** 箱号, 必填, 必须是已存在的集装箱(且未删除) */
    @NotBlank(message = "箱号不能为空")
    @Size(max = 30, message = "箱号长度不能超过 30")
    private String containerNo;

    /** 提空箱拖车号, 必填, 必须是已存在的拖车 */
    @NotBlank(message = "拖车号不能为空")
    @Size(max = 30, message = "拖车号长度不能超过 30")
    private String trackNo;

    /** 提空箱拖车进场时间, 可空 */
    private LocalDateTime dcInDate;

    /** 提空箱拖车出场时间, 可空 */
    private LocalDateTime dcOutDate;

    /** 箱使用备注, 可空 */
    @Size(max = 255, message = "箱使用备注长度不能超过 255")
    private String useNote;
}
