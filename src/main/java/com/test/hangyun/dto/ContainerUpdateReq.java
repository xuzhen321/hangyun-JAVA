package com.test.hangyun.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改集装箱请求。语义是整体覆盖: 未传的字段会被置为 null。
 * <p>
 * ⚠️ **没有 no 字段** —— 箱号是主键, 不可修改。要"换箱号"只能删了重建。
 * 前端做编辑表单时, 箱号那一栏应该是**只读的**。
 * <p>
 * 两个标志位在**接口上一律用 true/false**, 不要传 '0'/'1' —— 转换由后端做。
 */
@Data
public class ContainerUpdateReq {

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
