package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ContainerTrailerRecordQueryReq;
import com.test.hangyun.dto.ContainerTrailerRecordReq;
import com.test.hangyun.dto.vo.ContainerTrailerRecordVO;

import java.util.List;

public interface ContainerTrailerRecordService {

    /** 分页查询, 支持按箱号/拖车号前缀、进场时间区间筛选 */
    PageResult<ContainerTrailerRecordVO> page(ContainerTrailerRecordQueryReq req);

    /** 详情 */
    ContainerTrailerRecordVO getById(Long id);

    /** 新增提空箱登记(箱号和拖车号都必须存在) */
    void create(ContainerTrailerRecordReq req);

    /** 修改(整体覆盖) */
    void update(Long id, ContainerTrailerRecordReq req);

    /** 删除。物理删除, 这张表没有下游引用 */
    void delete(Long id);

    /** 批量物理删除。宽松语义: 存在的删掉、不存在的忽略, 幂等 */
    void deleteBatch(List<Long> ids);
}
