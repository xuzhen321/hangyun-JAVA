package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.PortCreateReq;
import com.test.hangyun.dto.PortQueryReq;
import com.test.hangyun.dto.PortUpdateReq;
import com.test.hangyun.dto.vo.PortOptionVO;
import com.test.hangyun.dto.vo.PortVO;

import java.util.List;

/**
 * 港口。完整的资源(列表/详情/增删改) + 下拉框。
 */
public interface PortService {

    /** 分页查询, 支持按五字码/中英文名前缀搜索、按国家筛选 */
    PageResult<PortVO> page(PortQueryReq req);

    /** 按查询条件取不分页的全量列表, 供导出用。超过上限抛 400 */
    List<PortVO> listForExport(PortQueryReq req);

    /** 详情 */
    PortVO getById(Long id);

    /** 新增 */
    void create(PortCreateReq req);

    /** 修改 */
    void update(Long id, PortUpdateReq req);

    /** 逻辑删除: 把 state 置为 '3', 行还在 */
    void delete(Long id);

    /** 下拉框候选(给订单选起运港/目的港用): 最多 20 条, 排除已删除的 */
    List<PortOptionVO> options(String keyword);
}
