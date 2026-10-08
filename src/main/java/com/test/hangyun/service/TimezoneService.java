package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.TimezoneQueryReq;
import com.test.hangyun.dto.TimezoneReq;
import com.test.hangyun.dto.vo.TimezoneOptionVO;
import com.test.hangyun.dto.vo.TimezoneVO;

import java.util.List;

public interface TimezoneService {

    PageResult<TimezoneVO> page(TimezoneQueryReq req);

    TimezoneVO getById(Long id);

    void create(TimezoneReq req);

    void update(Long id, TimezoneReq req);

    /** 删除, 有港口在用这个时区时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部 */
    List<TimezoneOptionVO> options();
}
