package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 港口信息视图 v_port （只读）。
 * <p>
 * 视图把**国家、区域、时区、尺寸、级别、类型、母港**的名称都联好了, 所以列表/详情不用 Service 组装。
 * 六组 xxx_id 也在视图里(后补的列, 见 viewInitial.sql)。
 * <p>
 * ⚠️ 视图里主键列叫 {@code port_id}(不是 id), 两个时区列叫 {@code timezone} / {@code timezone2}。
 */
@Data
@TableName("v_port")
public class PortView {

    /** 视图里主键列叫 port_id */
    @TableId(value = "port_id", type = IdType.INPUT)
    private Long portId;

    /** 五字码 */
    private String portUnlocode;

    /** 英文名 */
    private String portEnname;

    /** 中文名 */
    private String portCnname;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String geom;
    private String province;

    // ---- 六组字典的 id(后补的列) ----
    private Long countryId;
    private Long areaId;
    private Long timezoneId;
    private Long harborSizeId;
    private Long levelId;
    private Long portTypeId;

    // ---- 六组字典的名称(视图联好的) ----
    private String countryCode;
    private String countryEnname;
    private String countryCnname;
    private String areaName;
    private String harborSize;
    private Integer level;
    private String portType;

    /** 母港ID */
    private Long parentPortId;

    /** 母港中文名 */
    private String parentPortName;

    /** 港口时区 */
    @TableField("timezone")
    private String timezone;

    /** 港口时区(+8) */
    @TableField("timezone2")
    private String timezone2;

    /** 数据状态: 0默认, 1新增, 2修改, 3删除 */
    private String state;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
