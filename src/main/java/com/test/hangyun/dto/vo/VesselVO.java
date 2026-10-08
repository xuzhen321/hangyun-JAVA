package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.view.VesselView;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶响应对象。
 * <p>
 * 全部字段都来自**视图 v_vessel** —— 船旗国、船型、船东、管理公司的名称它都联好了,
 * 四个 xxx_id 也是从视图取的（那四列是后补进视图的, 见 viewInitial.sql）。
 **/
@Data
public class VesselVO {

    private Long id;

    /** 船名(视图里叫 shipname) */
    private String shipname;

    /** MMSI 号码 */
    private String mmsi;

    /** IMO 号码 */
    private String imo;

    /** 呼号 */
    private String callsign;

    // ---- 下面四个 id 也在视图里（后补的列）----

    /** 船舶类型ID */
    private Long vesselTypeId;

    /** 船旗国ID */
    private Long countryId;

    /** 船东ID */
    private Long ownerCompanyId;

    /** 管理公司ID */
    private Long managerCompanyId;

    // ---- 下面是视图直接给的名称/规格 ----

    /** 船型名称 */
    private String shiptype;

    /** 船旗国中文名 */
    private String flagState;

    /** 船东名称 */
    private String shipOwner;

    /** 船东代码 */
    private String shipOwnerCode;

    /** 管理公司名称 */
    private String shipManager;

    /** 管理公司代码 */
    private String shipManagerCode;

    /** 总吨 */
    private BigDecimal gt;

    /** 净吨 */
    private BigDecimal nt;

    /** 载重吨 */
    private BigDecimal dwt;

    /** 长度 */
    private BigDecimal length;

    /** 宽度 */
    private BigDecimal width;

    /** 建造年份 */
    private Integer buildYear;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static VesselVO from(VesselView v) {
        if (v == null) {
            return null;
        }
        VesselVO vo = new VesselVO();
        vo.setId(v.getId());
        vo.setShipname(v.getShipname());
        vo.setMmsi(v.getMmsi());
        vo.setImo(v.getImo());
        vo.setCallsign(v.getCallsign());
        vo.setVesselTypeId(v.getVesselTypeId());
        vo.setCountryId(v.getCountryId());
        vo.setOwnerCompanyId(v.getOwnerCompanyId());
        vo.setManagerCompanyId(v.getManagerCompanyId());
        vo.setShiptype(v.getShiptype());
        vo.setFlagState(v.getFlagState());
        vo.setShipOwner(v.getShipOwner());
        vo.setShipOwnerCode(v.getShipOwnerCode());
        vo.setShipManager(v.getShipManager());
        vo.setShipManagerCode(v.getShipManagerCode());
        vo.setGt(v.getGt());
        vo.setNt(v.getNt());
        vo.setDwt(v.getDwt());
        vo.setLength(v.getLength());
        vo.setWidth(v.getWidth());
        vo.setBuildYear(v.getBuildYear());
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        return vo;
    }
}
