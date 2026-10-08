package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.VesselCreateReq;
import com.test.hangyun.dto.VesselQueryReq;
import com.test.hangyun.dto.VesselUpdateReq;
import com.test.hangyun.dto.vo.VesselOptionVO;
import com.test.hangyun.dto.vo.VesselVO;

import java.util.List;

public interface VesselService {

    /** 分页查询, 支持按船名/MMSI/IMO 前缀搜索、按船旗国/船型精确筛选 */
    PageResult<VesselVO> page(VesselQueryReq req);

    /** 详情 */
    VesselVO getById(Long id);

    /** 新增 */
    void create(VesselCreateReq req);

    /** 修改 */
    void update(Long id, VesselUpdateReq req);

    /** 删除, 有航次用了这艘船时拒绝 */
    void delete(Long id);

    /** 批量删除。严格语义: 只要有一艘被航次引用就整批拒绝; 不存在的 id 忽略 */
    void deleteBatch(List<Long> ids);

    /** 下拉框候选(给新增航次选船用): 船名/MMSI 前缀搜索, 最多 20 条 */
    List<VesselOptionVO> options(String keyword);
}
