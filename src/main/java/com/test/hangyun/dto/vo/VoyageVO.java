package com.test.hangyun.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 航次响应对象。
 * <p>
 * loadingPortName / dischargePortName / vslName **都不是 voyage 表里的列** ——
 * 库里没有 v_voyage 视图, 所以由 Service 层分别查 port、vessel 批量组装进来。
 */
@Data
public class VoyageVO {

    private Long id;

    /** 航次号 */
    private String no;

    /** 船舶ID */
    private Long vslId;

    /** 船名(由 Service 组装, 来自 vessel.name) */
    private String vslName;

    /** 起始港口ID */
    private Long loadingPortId;

    /** 起始港口中文名(由 Service 组装) */
    private String loadingPortName;

    /** 目的港口ID */
    private Long dischargePortId;

    /** 目的港口中文名(由 Service 组装) */
    private String dischargePortName;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
