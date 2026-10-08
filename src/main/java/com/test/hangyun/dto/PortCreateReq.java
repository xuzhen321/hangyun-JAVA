package com.test.hangyun.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增港口请求。
 * <p>
 * unlocode(五字码)在库里有唯一约束, 新增/修改会查重。
 * state 不在请求体里 —— 由后端维护(新增置 '0', 删除置 '3')。
 */
@Data
public class PortCreateReq {

    /** 港口五字码, 必填, 如 CNSHA, 最长 10, 不能重复 */
    @NotBlank(message = "港口五字码不能为空")
    @Size(max = 10, message = "港口五字码长度不能超过 10")
    private String unlocode;

    /** 港口中文名, 可空 */
    @Size(max = 100, message = "港口中文名长度不能超过 100")
    private String cnname;

    /** 港口英文名, 可空 */
    @Size(max = 100, message = "港口英文名长度不能超过 100")
    private String enname;

    /** 国家ID, 必须是已存在的国家(用 /countries/options 选) */
    private Long countryId;

    /** 区域ID, 必须是已存在的区域(用 /areas/options 选) */
    private Long areaId;

    /** 时区ID, 必须是已存在的时区(用 /timezones/options 选) */
    private Long timezoneId;

    /** 港口尺寸ID, 用 /harbor-sizes/options 选 */
    private Long harborSizeId;

    /** 港口级别ID, 用 /port-levels/options 选 */
    private Long levelId;

    /** 港口类型ID, 用 /port-types/options 选 */
    private Long portTypeId;

    /** 母港ID, 必须是已存在的港口(自反联系) */
    private Long parentPortId;

    /** 纬度, 对应 decimal(9,6): 最多 3 位整数 + 6 位小数 */
    @Digits(integer = 3, fraction = 6, message = "纬度最多 3 位整数、6 位小数")
    private BigDecimal latitude;

    /** 经度, 对应 decimal(9,6) */
    @Digits(integer = 3, fraction = 6, message = "经度最多 3 位整数、6 位小数")
    private BigDecimal longitude;

    /** 港口范围(WKT 格式) */
    @Size(max = 255, message = "港口范围长度不能超过 255")
    private String geom;

    /** 所在省份 */
    @Size(max = 50, message = "省份长度不能超过 50")
    private String province;
}
