package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CustomerStatusQueryReq;
import com.test.hangyun.dto.CustomerStatusReq;
import com.test.hangyun.dto.vo.CustomerStatusVO;

import java.util.List;

public interface CustomerStatusService {

    /** 分页查询 */
    PageResult<CustomerStatusVO> page(CustomerStatusQueryReq req);

    /** 详情 */
    CustomerStatusVO getById(Long id);

    /** 新增 */
    void create(CustomerStatusReq req);

    /** 修改 */
    void update(Long id, CustomerStatusReq req);

    /** 删除, 有客户引用该状态时拒绝 */
    void delete(Long id);

    /** 全部状态, 供前端下拉框使用 */
    List<CustomerStatusVO> listAll();
}
