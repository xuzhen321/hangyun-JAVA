package com.test.hangyun.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增船舶请求。
 * <p>
 * ⚠️ **必填的比想象中多**, 因为列表里这些列都不允许空白:
 * <ul>
 *   <li>{@code vesselTypeId} —— 列表的「船舶类型」以及「总吨/净吨/载重吨/长度/宽度」
 *       五列都是从**船型**联出来的, 没有船型这六列全空</li>
 *   <li>{@code countryId} —— 列表的「船旗国」</li>
 *   <li>{@code ownerCompanyId} / {@code managerCompanyId} —— 列表的「船东」「管理公司」,
 *       且这两家公司**必须已填代码**, 否则「船东代码」「管理公司代码」两列是空的(见 Service)</li>
 *   <li>{@code buildYear} —— 列表的「建造年份」, 是 vessel 表自己的列</li>
 * </ul>
 * <p>
 * ⚠️ mmsi / imo / callsign **也都必填**, 而且三列在库里**各有唯一约束** ——
 * 所以是"必填 + 全库唯一"。这三项是船舶的全球识别码(MMSI/IMO/呼号),
 * 真实船舶必有, 也是区分名称相近的船的依据。
 */
@Data
public class VesselCreateReq {

    /** 船名, 必填 */
    @NotBlank(message = "船名不能为空")
    @Size(max = 100, message = "船名长度不能超过 100")
    private String name;

    /** 船舶类型ID, 必填, 必须是 ship_type 中已存在的记录(用 /ship-types/options 选) */
    @NotNull(message = "船舶类型不能为空")
    private Long vesselTypeId;

    /** 船旗国ID, 必填, 必须是 country 中已存在的记录(用 /countries/options 选) */
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

    /** 船东ID, 必填, 必须是 company 中已存在**且填了代码**的记录(用 /companies/options 选) */
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
