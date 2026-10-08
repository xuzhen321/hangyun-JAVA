package com.test.hangyun.dto;

import lombok.Data;

/**
 * 船舶列表查询条件。
 */
@Data
public class VesselQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /**
     * 关键字, **前缀匹配**: 同时匹配**船名**、**MMSI**、**IMO**, 任一命中即返回。
     * 传 "中远" 能命中船名以"中远"开头的船, 传 "413" 能命中 MMSI 以 413 开头的船。
     */
    private String keyword;

    /** 船旗国ID, 精确匹配 */
    private Long countryId;

    /** 船舶类型ID, 精确匹配 */
    private Long vesselTypeId;
}
