package com.test.hangyun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增集装箱请求。
 * <p>
 * 箱号是业务主键(如 SEGU9481570), 由**前端提供**, 后端不生成; 所以新增必填、且不可重复。
 * 修改时箱号不能改 —— 见 {@link ContainerUpdateReq}, 那里没有这个字段。
 * <p>
 * 两个标志位在**接口上一律用 true/false**, 不要传 '0'/'1' ——
 * 库里存的是 char(1), 转换由后端做(见 ContainerServiceImpl)。
 */
@Data
public class ContainerCreateReq {

    /** 箱号, 必填, 最长 30 */
    @NotBlank(message = "箱号不能为空")
    @Size(max = 30, message = "箱号长度不能超过 30")
    private String no;

    /** 箱型ID, 必须是 container_type 中已存在的记录 */
    private Long typeId;

    /** 箱主ID, 必须是 company 中已存在的记录 */
    private Long ownerId;

    /** 操作方ID, 必须是 company 中已存在的记录 */
    private Long operatorId;

    /** 铅封号 */
    @Size(max = 50, message = "铅封号长度不能超过 50")
    private String sealNo;

    /** 当前状态ID, 必须是 container_status 中已存在的记录 */
    private Long statusId;

    /** 是否危险品。接口用 true/false, 库里存 '1'/'0' */
    private Boolean dangerFlag;

    /** 是否海事标识。接口用 true/false, 库里存 '1'/'0' */
    private Boolean maritimeFlag;

    /** 船公司操作: 加锁、截关等 */
    @Size(max = 10, message = "船公司操作长度不能超过 10")
    private String carrierOperate;

    /** 集装箱堆场状态 */
    @Size(max = 50, message = "堆场状态长度不能超过 50")
    private String ctrStatusTerminal;
}
