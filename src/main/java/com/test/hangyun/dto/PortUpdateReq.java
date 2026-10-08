package com.test.hangyun.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改港口请求。语义是整体覆盖: 未传的字段会被置为 null。
 * <p>
 * 字段和新增一样(分开两个类是为了和项目里其他资源保持一致)。
 * ⚠️ **state 不在请求体里**: 它由后端维护(新增 '0'、删除 '3'), 修改时不动它 ——
 * 否则前端一个疏忽就能把已删除的港口"复活"。
 */
@Data
public class PortUpdateReq {

    /** 港口五字码, 必填, 不能重复 */
    @NotBlank(message = "港口五字码不能为空")
    @Size(max = 10, message = "港口五字码长度不能超过 10")
    private String unlocode;

    /** 港口中文名 */
    @Size(max = 100, message = "港口中文名长度不能超过 100")
    private String cnname;

    /** 港口英文名 */
    @Size(max = 100, message = "港口英文名长度不能超过 100")
    private String enname;

    private Long countryId;

    private Long areaId;

    private Long timezoneId;

    private Long harborSizeId;

    private Long levelId;

    private Long portTypeId;

    /** 母港ID, 不能指向自己 */
    private Long parentPortId;

    @Digits(integer = 3, fraction = 6, message = "纬度最多 3 位整数、6 位小数")
    private BigDecimal latitude;

    @Digits(integer = 3, fraction = 6, message = "经度最多 3 位整数、6 位小数")
    private BigDecimal longitude;

    @Size(max = 255, message = "港口范围长度不能超过 255")
    private String geom;

    @Size(max = 50, message = "省份长度不能超过 50")
    private String province;
}
