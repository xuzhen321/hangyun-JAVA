package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.EventStatusQueryReq;
import com.test.hangyun.dto.EventStatusReq;
import com.test.hangyun.dto.vo.EventStatusOptionVO;
import com.test.hangyun.dto.vo.EventStatusVO;

import java.util.List;

public interface EventStatusService {

    /** 分页查询 */
    PageResult<EventStatusVO> page(EventStatusQueryReq req);

    /** 详情 */
    EventStatusVO getById(Long id);

    /** 新增 */
    void create(EventStatusReq req);

    /** 修改 */
    void update(Long id, EventStatusReq req);

    /** 删除, 有物流事件在用这个状态时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部(字典表条数少) */
    List<EventStatusOptionVO> options();
}
