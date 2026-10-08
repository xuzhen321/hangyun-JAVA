package com.test.hangyun.dto.vo;

import lombok.Data;

/**
 * 集装箱概览: 基础信息 + 最近一条物流事件。
 * <p>
 * 对应文档 5.3 的 {@code GET /containers/{no}/summary}。
 */
@Data
public class ContainerSummaryVO {

    /** 集装箱基础信息(含箱型、箱主、操作方、状态描述 —— 和 12.2 的详情一样) */
    private ContainerVO container;

    /** 最近一条物流事件; 该箱还没有任何事件时为 null */
    private ContainerEventVO latestEvent;

    public static ContainerSummaryVO of(ContainerVO container, ContainerEventVO latestEvent) {
        ContainerSummaryVO vo = new ContainerSummaryVO();
        vo.setContainer(container);
        vo.setLatestEvent(latestEvent);
        return vo;
    }
}
