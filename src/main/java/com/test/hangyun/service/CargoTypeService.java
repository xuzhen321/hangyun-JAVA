package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CargoTypeQueryReq;
import com.test.hangyun.dto.CargoTypeReq;
import com.test.hangyun.dto.vo.CargoTypeVO;

import java.util.List;

public interface CargoTypeService {

    /** 分页查询, 支持按名称前缀筛选 */
    PageResult<CargoTypeVO> page(CargoTypeQueryReq req);

    /** 详情 */
    CargoTypeVO getById(Long id);

    /** 新增 */
    void create(CargoTypeReq req);

    /** 修改 */
    void update(Long id, CargoTypeReq req);

    /** 删除, 有货物引用该种类时拒绝 */
    void delete(Long id);

    /**
     * 批量删除。
     * <p>
     * 严格语义: 只要有一个被货物引用, 整批拒绝(409), 不做部分删除。
     * 不存在的 id 会被忽略(幂等)。
     */
    void deleteBatch(List<Long> ids);
}
