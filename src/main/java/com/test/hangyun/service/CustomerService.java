package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CustomerCreateReq;
import com.test.hangyun.dto.CustomerQueryReq;
import com.test.hangyun.dto.CustomerUpdateReq;
import com.test.hangyun.dto.vo.CustomerOptionVO;
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

    /**
     * 下拉框候选: 按姓名前缀搜索, 最多 20 条, 已注销的客户不出现。
     * name 为空时返回前 20 条, 便于前端打开下拉框时先展示一批。
     */
    List<CustomerOptionVO> options(String name);
}
