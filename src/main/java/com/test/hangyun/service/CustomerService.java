package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CustomerCreateReq;
import com.test.hangyun.dto.CustomerQueryReq;
import com.test.hangyun.dto.CustomerUpdateReq;
import com.test.hangyun.dto.vo.CustomerVO;

import java.util.List;

public interface CustomerService {

    /** 分页查询(读视图 v_customer) */
    PageResult<CustomerVO> page(CustomerQueryReq req);

    /** 详情(读视图 v_customer) */
    CustomerVO getById(Long id);

    /** 新增(写表 customer), 不返回数据 */
    void create(CustomerCreateReq req);

    /** 修改(写表 customer), 不返回数据 */
    void update(Long id, CustomerUpdateReq req);

    /** 删除(写表 customer) */
    void delete(Long id);

    /** 批量删除(写表 customer), 不存在的 id 忽略 */
    void deleteBatch(List<Long> ids);
}
