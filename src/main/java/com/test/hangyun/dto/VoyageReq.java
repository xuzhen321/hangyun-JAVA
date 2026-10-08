package com.test.hangyun.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 航次的新增/修改请求。
 * <p>
 * 两个操作语义相同(整体覆盖), 所以共用一个 DTO。
 * 注意: 不含 insert_time / update_time —— 由数据库触发器维护。
 * <p>
 * ⚠️ 航次号**不是全库唯一**: 航次号由船公司自编, 不同公司的船大量重号(如 001E 每年复用)。
 * 唯一的是 **(vsl_id, no) 这一对** —— 同一艘船不能有两个同号航次, 不同船可以重号。
 */
@Data
public class VoyageReq {

    /** 航次号, 可空, 如 2026E001。与 vslId 一起构成业务唯一键(见类注释) */
    @Size(max = 20, message = "航次号长度不能超过 20")
    private String no;

    /** 船舶ID, 必须是 vessel 中已存在的记录(用 /vessels/options 选) */
    private Long vslId;

    /** 起始港口ID, 必须是 port 中已存在的记录 */
    private Long loadingPortId;

    /** 目的港口ID, 必须是 port 中已存在的记录 */
    private Long dischargePortId;
}
