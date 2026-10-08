package com.test.hangyun.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改船舶请求。语义是整体覆盖: 未传的字段会被置为 null。
 * <p>
 * 和新增的字段完全一样(船本身就是这些属性), 所以两个 DTO 长得一样, 但**是分开的两个类** ——
 * 和项目里其他资源保持一致(CustomerCreateReq / CustomerUpdateReq 也是分开的)。
 * <p>
 * ⚠️ 必填项和新增完全一致(见 {@link VesselCreateReq} 的类注释): 修改也是整体覆盖,
 * 少传一个就把它清成 null, 列表对应那一列就空白了, 所以两边的约束必须一样。
 */
@Data
public class VesselUpdateReq {

    /** 船名, 必填 */
    @NotBlank(message = "船名不能为空")
    @Size(max = 100, message = "船名长度不能超过 100")
    private String name;

    /** 船舶类型ID, 必填, 必须是 ship_type 中已存在的记录 */
    @NotNull(message = "船舶类型不能为空")
    private Long vesselTypeId;

    /** 船旗国ID, 必填, 必须是 country 中已存在的记录 */
    @NotNull(message = "船旗国不能为空")
    private Long countryId;

    /** MMSI 号码, 必填, 最长 20, 不能和别的船重复 */
    @NotBlank(message = "MMSI 不能为空")
    @Size(max = 20, message = "MMSI 长度不能超过 20")
    private String mmsi;

    /** IMO 号码, 必填, 最长 20, 不能和别的船重复 */
    @NotBlank(message = "IMO 不能为空")
    @Size(max = 20, message = "IMO 长度不能超过 20")
    private String imo;

    /** 建造年份, 必填 */
    @NotNull(message = "建造年份不能为空")
    @Min(value = 1900, message = "建造年份不合法")
    @Max(value = 2100, message = "建造年份不合法")
    private Integer buildYear;

    /** 船东ID, 必填, 必须是 company 中已存在**且填了代码**的记录 */
    @NotNull(message = "船东不能为空")
    private Long ownerCompanyId;

    /** 管理公司ID, 必填, 要求同上 */
    @NotNull(message = "管理公司不能为空")
    private Long managerCompanyId;

    /** 呼号, 必填, 最长 20, 不能和别的船重复 */
    @NotBlank(message = "呼号不能为空")
    @Size(max = 20, message = "呼号长度不能超过 20")
    private String callsign;
}
