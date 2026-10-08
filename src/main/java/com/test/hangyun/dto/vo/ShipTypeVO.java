package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ShipType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶类型响应对象。
 * <p>
 * 库里列名是 {@code "GT"} / {@code "NT"} / {@code "DWT"}（大写带引号），
 * 接口层统一用小写的 {@code gt} / {@code nt} / {@code dwt}，前端按小驼峰取就行。
 */
@Data
public class ShipTypeVO {

    private Long id;

    /** 船舶类型名称 */
    private String type;

    /** 总吨 */
    private BigDecimal gt;

    /** 长度 */
    private BigDecimal length;

    /** 宽度 */
    private BigDecimal width;

    /** 净吨 */
    private BigDecimal nt;

    /** 载重吨 */
    private BigDecimal dwt;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static ShipTypeVO from(ShipType e) {
        if (e == null) {
            return null;
        }
        ShipTypeVO vo = new ShipTypeVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setGt(e.getGt());
        vo.setLength(e.getLength());
        vo.setWidth(e.getWidth());
        vo.setNt(e.getNt());
        vo.setDwt(e.getDwt());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
