package com.test.hangyun.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流事件列表查询条件。
 * <p>
 * 所有条件都可选, 同时传就是 AND。
 */
@Data
public class ContainerEventQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 箱号, **精确匹配**(箱号是标识符, 用户一般整串复制; 也走已有的外键索引) */
    private String containerNo;

    /** 事件状态ID, 精确匹配 */
    private Long eventStatusId;

    /** 发生时间下界(含) */
    private LocalDateTime eventTimeFrom;

    /** 发生时间上界(含) */
    private LocalDateTime eventTimeTo;
}
