package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ContainerEventQueryReq;
import com.test.hangyun.dto.ContainerEventReq;
import com.test.hangyun.dto.vo.ContainerEventVO;

import java.util.List;

public interface ContainerEventService {

    /** 分页查询, 支持按箱号(精确)、事件状态(精确)、发生时间区间筛选 */
    PageResult<ContainerEventVO> page(ContainerEventQueryReq req);

    /** 按查询条件取不分页的全量列表, 供导出用。超过上限抛 400 */
    List<ContainerEventVO> listForExport(ContainerEventQueryReq req);

    /** 详情 */
    ContainerEventVO getById(Long id);

    /** 新增事件 */
    void create(ContainerEventReq req);

    /** 修改事件 */
    void update(Long id, ContainerEventReq req);

    /** 逻辑删除: 把状态改成"已删除" */
    void delete(Long id);

    /** 批量逻辑删除。宽松语义: 存在的改状态、不存在的忽略, 幂等 */
    void deleteBatch(List<Long> ids);

    /**
     * 集装箱轨迹: 按箱号查出**全部**事件, 按发生时间**升序**返回（时间轴顺序）。
     * 已删除的事件不返回。
     */
    List<ContainerEventVO> track(String containerNo);

    /** 该集装箱最近的一条事件, 没有就返回 null（给"集装箱概览"用） */
    ContainerEventVO latestEvent(String containerNo);
}
