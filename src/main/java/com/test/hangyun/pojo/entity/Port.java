package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 港口信息表 port （写用）。
 * <p>
 * 被订单(起运港/目的港)、物流事件(发生地)、航次(起始/目的港)、船舶(无)等多处引用。
 * <p>
 * 港口是**逻辑删除**: 把 state 置为 '3'(删除), 行还在, 那些引用就不会悬空。
 * state 取值: 0默认, 1新增, 2修改, 3删除。
 * <p>
 * 六个字典引用(country_id / area_id / timezone_id / harbor_size_id / level_id /
 * port_type_id)和一个自引用(parent_port_id 母港)都是逻辑外键, 库里没有物理约束。
 */
@Data
@TableName("port")
public class Port {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 港口五字码, 如 CNSHA。库里有唯一约束 */
    private String unlocode;

    /** 港口英文名 */
    private String enname;

    /** 港口中文名 */
    private String cnname;

    /** 国家ID, 逻辑外键 -> country.id */
    private Long countryId;

    /** 区域ID, 逻辑外键 -> area.id */
    private Long areaId;

    /** 时区ID, 逻辑外键 -> timezone.id */
    private Long timezoneId;

    /** 港口尺寸ID, 逻辑外键 -> harbor_size.id */
    private Long harborSizeId;

    /** 港口级别ID, 逻辑外键 -> port_level.id */
    private Long levelId;

    /** 港口类型ID, 逻辑外键 -> port_type.id */
    private Long portTypeId;

    /** 母港ID, 逻辑外键 -> port.id(自反联系) */
    private Long parentPortId;

    /** 港口中心点纬度 */
    private BigDecimal latitude;

    /** 港口中心点经度 */
    private BigDecimal longitude;

    /** 港口范围(WKT 格式) */
    private String geom;

    /** 港口所在省份 */
    private String province;

    /** 数据状态: 0默认, 1新增, 2修改, 3删除 */
    private String state;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
