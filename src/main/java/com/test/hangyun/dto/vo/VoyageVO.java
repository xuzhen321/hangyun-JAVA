package com.test.hangyun.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 航次响应对象。
 * <p>
 * loadingPortName / dischargePortName **不是 voyage 表里的列** —— 库里没有 v_voyage 视图,
 * 所以由 Service 层查 port 组装进来。
 * <p>
 * ⚠️ **没有船名**：vslId 指向 vessel, 但船舶模块还没做, 所以只有裸 id、没有 vslName。
 * 等第 6 个模块做完再补。
 */
@Data
public class VoyageVO {

    private Long id;

    /** 航次号 */
    private String no;

    /** 船舶ID(暂时只有 id, 没有船名) */
    private Long vslId;

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
