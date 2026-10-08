package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ShipTypeQueryReq;
import com.test.hangyun.dto.ShipTypeReq;
import com.test.hangyun.dto.vo.ShipTypeOptionVO;
import com.test.hangyun.dto.vo.ShipTypeVO;

import java.util.List;

public interface ShipTypeService {

    /** 分页查询, 支持按类型名称前缀搜索 */
    PageResult<ShipTypeVO> page(ShipTypeQueryReq req);

    /** 详情 */
    ShipTypeVO getById(Long id);

    /** 新增 */
    void create(ShipTypeReq req);

    /** 修改 */
    void update(Long id, ShipTypeReq req);

    /** 删除, 有船舶在用这个船型时拒绝 */
    void delete(Long id);

    /** 下拉框候选(给新增船舶选船型用): 一次性返回全部(字典表条数少) */
    List<ShipTypeOptionVO> options();
}
