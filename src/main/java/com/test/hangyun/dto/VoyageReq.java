package com.test.hangyun.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 航次的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护。
 */
@Data
public class VoyageReq {

    /** 航次号, 可空, 如 2026E001。库里没有唯一约束, 所以不查重 */
    @Size(max = 20, message = "航次号长度不能超过 20")
    private String no;

    /**
     * 船舶ID。
     * ⚠️ 目前**不校验**是否存在 —— 船舶模块还没做, vessel 表是空的, 校验了会挡住所有数据。
     */
    private Long vslId;

    /** 起始港口ID, 必须是 port 中已存在的记录 */
    private Long loadingPortId;

    /** 目的港口ID, 必须是 port 中已存在的记录 */
    private Long dischargePortId;
}
