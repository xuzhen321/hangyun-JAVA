package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 船舶信息表 vessel （写用）。
 * <p>
 * 报告中的关系模式: Vessel(id, name, vessel_type_id, country_id, mmsi, imo, build_year,
 *                          owner_company_id, manager_company_id, callsign)
 * <p>
 * mmsi / imo / callsign 三列在库里**都有唯一约束**, 所以新增/修改时都要查重。
 * 被 voyage.vsl_id 引用(航次), 删除前要查引用。
 */
@Data
@TableName("vessel")
public class Vessel {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 船舶名称 */
    private String name;

    /** 船舶类型ID, 逻辑外键 -> ship_type.id */
    private Long vesselTypeId;

    /** 船旗国ID, 逻辑外键 -> country.id */
    private Long countryId;

    /** MMSI 号码。库里有唯一约束 */
    private String mmsi;

    /** IMO 号码。库里有唯一约束 */
    private String imo;

    /** 建造年份 */
    private Integer buildYear;

    /** 船东ID, 逻辑外键 -> company.id */
    private Long ownerCompanyId;

    /** 管理公司ID, 逻辑外键 -> company.id */
    private Long managerCompanyId;

    /** 呼号(无线电呼号)。库里有唯一约束 */
    private String callsign;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
