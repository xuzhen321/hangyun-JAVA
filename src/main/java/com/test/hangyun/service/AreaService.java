package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.AreaQueryReq;
import com.test.hangyun.dto.AreaReq;
import com.test.hangyun.dto.vo.AreaOptionVO;
import com.test.hangyun.dto.vo.AreaVO;

import java.util.List;

public interface AreaService {

    PageResult<AreaVO> page(AreaQueryReq req);

    AreaVO getById(Long id);

    void create(AreaReq req);

    void update(Long id, AreaReq req);

    /** 删除, 有港口在用这个区域时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部 */
    List<AreaOptionVO> options();
}
