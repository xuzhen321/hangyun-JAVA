package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.PortTypeQueryReq;
import com.test.hangyun.dto.PortTypeReq;
import com.test.hangyun.dto.vo.PortTypeOptionVO;
import com.test.hangyun.dto.vo.PortTypeVO;

import java.util.List;

public interface PortTypeService {

    PageResult<PortTypeVO> page(PortTypeQueryReq req);

    PortTypeVO getById(Long id);

    void create(PortTypeReq req);

    void update(Long id, PortTypeReq req);

    /** 删除, 有港口在用这个类型时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部 */
    List<PortTypeOptionVO> options();
}
