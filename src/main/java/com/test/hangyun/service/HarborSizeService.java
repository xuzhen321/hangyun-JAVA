package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.HarborSizeQueryReq;
import com.test.hangyun.dto.HarborSizeReq;
import com.test.hangyun.dto.vo.HarborSizeOptionVO;
import com.test.hangyun.dto.vo.HarborSizeVO;

import java.util.List;

public interface HarborSizeService {

    PageResult<HarborSizeVO> page(HarborSizeQueryReq req);

    HarborSizeVO getById(Long id);

    void create(HarborSizeReq req);

    void update(Long id, HarborSizeReq req);

    /** 删除, 有港口在用这个尺寸时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部 */
    List<HarborSizeOptionVO> options();
}
