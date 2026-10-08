package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.OperationLogQueryReq;
import com.test.hangyun.dto.vo.OperationLogVO;

import java.util.List;

/**
 * 操作日志。**只读** —— 不提供增删改接口。
 * <p>
 * 审计记录只进不出: 允许修改或删除, 审计就失去意义了。
 * 写入由 {@code @OpLog} + 切面自动完成, 没有手工新增入口。
 */
public interface OperationLogService {

    /** 分页查询, 支持按操作人/类型/目标表/结果/时间范围筛选 */
    PageResult<OperationLogVO> page(OperationLogQueryReq req);

    /** 详情 */
    OperationLogVO getById(Long id);

    /**
     * 按查询条件取**不分页**的全量列表, 供导出用。
     * <p>
     * 会先按上限做一次探测: 超过 {@code ExportConstants.MAX_ROWS} 行直接抛 400,
     * 让用户缩小筛选范围, 而不是把库拖垮。
     */
    List<OperationLogVO> listForExport(OperationLogQueryReq req);
}
