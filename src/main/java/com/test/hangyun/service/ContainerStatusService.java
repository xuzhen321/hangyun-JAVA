package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ContainerStatusQueryReq;
import com.test.hangyun.dto.ContainerStatusReq;
import com.test.hangyun.dto.vo.ContainerStatusOptionVO;
import com.test.hangyun.dto.vo.ContainerStatusVO;

import java.util.List;

public interface ContainerStatusService {

    /** 分页查询 */
    PageResult<ContainerStatusVO> page(ContainerStatusQueryReq req);

    /** 详情 */
    ContainerStatusVO getById(Long id);

    /** 新增 */
    void create(ContainerStatusReq req);

    /** 修改 */
    void update(Long id, ContainerStatusReq req);

    /** 删除, 有集装箱在用这个状态时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部(字典表条数少) */
    List<ContainerStatusOptionVO> options();
}
