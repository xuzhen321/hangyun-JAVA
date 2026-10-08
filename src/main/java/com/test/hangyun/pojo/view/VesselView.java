package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶信息视图 v_vessel （只读）。
 * <p>
 * 视图把**船旗国、船型、船东、管理公司的名称**都联好了, 所以列表/详情不用 Service 层组装。
 * <p>
 * ⚠️ **那四个 xxx_id 是后补到视图里的**（原来的 v_vessel 只有名称）——
 * 光有名称的话, 前端编辑表单没法回填下拉框, 也没法按船旗国/船型筛选。
 * <p>
 * ⚠️ 三个带引号的列名: 视图里就是 {@code "GT"} / {@code "NT"} / {@code "DWT"}（大写）,
 * 必须用 {@code @TableField("\"...\"")} 显式写出来, 否则 MP 生成不带引号的列名会被 PG 折叠成小写。
 * <p>
 * 对应列: id, mmsi, shipname, imo, callsign,
 *         vessel_type_id, country_id, owner_company_id, manager_company_id,
 *         flag_state, shiptype, "GT", "NT", "DWT", length, width, build_year,
 *         ship_owner, ship_owner_code, ship_manager, ship_manager_code, insert_time, update_time
 */
@Data
@TableName("v_vessel")
public class VesselView {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String mmsi;

    /** 船名。注意视图里这一列叫 shipname, 不是 name */
    private String shipname;

    private String imo;

    private String callsign;

    /** 船舶类型ID */
    private Long vesselTypeId;

    /** 船旗国ID */
    private Long countryId;

    /** 船东ID */
    private Long ownerCompanyId;

    /** 管理公司ID */
    private Long managerCompanyId;

    /** 船旗国中文名(来自 country.country_cnname) */
    private String flagState;

    /** 船型名称(来自 ship_type.type) */
    private String shiptype;

    /** 总吨 */
    @TableField("\"GT\"")
    private BigDecimal gt;

    /** 净吨 */
    @TableField("\"NT\"")
    private BigDecimal nt;

    /** 载重吨 */
    @TableField("\"DWT\"")
    private BigDecimal dwt;

    /** 长度 */
    private BigDecimal length;

    /** 宽度 */
    private BigDecimal width;

    /** 建造年份 */
    private Integer buildYear;

    /** 船东名称(来自 company.name) */
    private String shipOwner;

    /** 船东代码(来自 company.code) */
    private String shipOwnerCode;

    /** 管理公司名称 */
    private String shipManager;

    /** 管理公司代码 */
    private String shipManagerCode;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
