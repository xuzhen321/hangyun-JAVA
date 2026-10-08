package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.PortLevelQueryReq;
import com.test.hangyun.dto.PortLevelReq;
import com.test.hangyun.dto.vo.PortLevelOptionVO;
import com.test.hangyun.dto.vo.PortLevelVO;

import java.util.List;

public interface PortLevelService {

    PageResult<PortLevelVO> page(PortLevelQueryReq req);

    PortLevelVO getById(Long id);

    void create(PortLevelReq req);

    void update(Long id, PortLevelReq req);

    /** 删除, 有港口在用这个级别时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部 */
    List<PortLevelOptionVO> options();
}
