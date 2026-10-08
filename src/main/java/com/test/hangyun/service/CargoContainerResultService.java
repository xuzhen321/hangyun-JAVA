package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CargoContainerResultQueryReq;
import com.test.hangyun.dto.CargoContainerResultReq;
import com.test.hangyun.dto.vo.CargoContainerResultVO;

import java.util.List;

public interface CargoContainerResultService {

    /** 分页查询, 支持按货物名称(前缀)、订单号(精确)、集装箱号(精确)筛选 */
    PageResult<CargoContainerResultVO> page(CargoContainerResultQueryReq req);

    /** 详情 */
    CargoContainerResultVO getById(Long id);

    /** 新增(货物、箱号、数量都必填) */
    void create(CargoContainerResultReq req);

    /** 修改(整体覆盖: 三个字段都必填) */
    void update(Long id, CargoContainerResultReq req);

    /** 删除。物理删除, 删掉就真的没了 */
    void delete(Long id);

    /** 批量删除。物理删除; 不存在的 id 忽略 */
    void deleteBatch(List<Long> ids);
}
