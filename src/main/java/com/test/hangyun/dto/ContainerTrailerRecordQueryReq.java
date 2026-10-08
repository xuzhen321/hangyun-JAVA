package com.test.hangyun.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提空箱记录列表查询条件。
 * <p>
 * 所有条件都可选, 同时传就是 AND。
 */
@Data
public class ContainerTrailerRecordQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 箱号, 前缀匹配 */
    private String containerNo;

    /** 提空箱拖车号, 前缀匹配 */
    private String trackNo;

    /** 进场时间下界(含), 与 dcInDateTo 组成闭区间 */
    private LocalDateTime dcInDateFrom;

    /** 进场时间上界(含), 与 dcInDateFrom 组成闭区间 */
    private LocalDateTime dcInDateTo;
}
