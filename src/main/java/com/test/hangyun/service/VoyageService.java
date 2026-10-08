package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.VoyageQueryReq;
import com.test.hangyun.dto.VoyageReq;
import com.test.hangyun.dto.vo.VoyageVO;

import java.util.List;

public interface VoyageService {

    /** 分页查询, 支持按航次号前缀、起始港口筛选 */
    PageResult<VoyageVO> page(VoyageQueryReq req);

    /** 按查询条件取不分页的全量列表, 供导出用。超过上限抛 400 */
    List<VoyageVO> listForExport(VoyageQueryReq req);

    /** 详情 */
    VoyageVO getById(Long id);

    /** 新增 */
    void create(VoyageReq req);

    /** 修改 */
    void update(Long id, VoyageReq req);

    /** 删除, 有物流事件挂在这个航次时拒绝 */
    void delete(Long id);
}
