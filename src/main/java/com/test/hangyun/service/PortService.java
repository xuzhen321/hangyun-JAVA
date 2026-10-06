package com.test.hangyun.service;

import com.test.hangyun.dto.vo.PortOptionVO;

import java.util.List;

/**
 * 港口。目前只有下拉框搜索, 完整的 /ports 资源(列表、详情、增删改)以后再做。
 */
public interface PortService {

    /**
     * 下拉框候选: 关键词同时匹配**中文名 / 英文名 / 五字码**的前缀, 最多 20 条,
     * 已删除(state = '3')的港口不出现。
     * keyword 为空时返回前 20 条。
     */
    List<PortOptionVO> options(String keyword);
}
