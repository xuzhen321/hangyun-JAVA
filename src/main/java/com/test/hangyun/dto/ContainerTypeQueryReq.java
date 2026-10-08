package com.test.hangyun.dto;

import lombok.Data;

/**
 * 集装箱箱型列表查询条件。
 * <p>
 * 字典表, 分页查询只接受页码和条数; 需要取全集给下拉框用请走 /container-types/options。
 */
@Data
public class ContainerTypeQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;
}
