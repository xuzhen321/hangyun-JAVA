package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.view.PortView;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 港口响应对象。
 * <p>
 * 全部字段都来自**视图 v_port** —— 国家、区域、时区、尺寸、级别、类型、母港的名称它都联好了,
 * 六组 xxx_id 也在视图里(后补的列)。
 */
@Data
public class PortVO {

    /** 港口ID(视图里叫 port_id) */
    private Long id;

    private String unlocode;
    private String cnname;
    private String enname;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String geom;
    private String province;

    // ---- 六组字典的 id ----
    private Long countryId;
    private Long areaId;
    private Long timezoneId;
    private Long harborSizeId;
    private Long levelId;
    private Long portTypeId;

    // ---- 六组字典的名称 ----
    private String countryCode;
    private String countryCnname;
    private String countryEnname;
    private String areaName;
    private String harborSize;
    private Integer level;
    private String portType;

    /** 母港ID */
    private Long parentPortId;

    /** 母港中文名 */
    private String parentPortName;

    /** 港口时区 */
    private String timezone;

    /** 港口时区(+8) */
    private String timezone2;

    /** 数据状态: 0默认, 1新增, 2修改, 3删除 */
    private String state;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static PortVO from(PortView v) {
        if (v == null) {
            return null;
        }
        PortVO vo = new PortVO();
        vo.setId(v.getPortId());
        vo.setUnlocode(v.getPortUnlocode());
        vo.setCnname(v.getPortCnname());
        vo.setEnname(v.getPortEnname());
        vo.setLatitude(v.getLatitude());
        vo.setLongitude(v.getLongitude());
        vo.setGeom(v.getGeom());
        vo.setProvince(v.getProvince());
        vo.setCountryId(v.getCountryId());
        vo.setAreaId(v.getAreaId());
        vo.setTimezoneId(v.getTimezoneId());
        vo.setHarborSizeId(v.getHarborSizeId());
        vo.setLevelId(v.getLevelId());
        vo.setPortTypeId(v.getPortTypeId());
        vo.setCountryCode(v.getCountryCode());
        vo.setCountryCnname(v.getCountryCnname());
        vo.setCountryEnname(v.getCountryEnname());
        vo.setAreaName(v.getAreaName());
        vo.setHarborSize(v.getHarborSize());
        vo.setLevel(v.getLevel());
        vo.setPortType(v.getPortType());
        vo.setParentPortId(v.getParentPortId());
        vo.setParentPortName(v.getParentPortName());
        vo.setTimezone(v.getTimezone());
        vo.setTimezone2(v.getTimezone2());
        vo.setState(v.getState());
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        return vo;
    }
}
