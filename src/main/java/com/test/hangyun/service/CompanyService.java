package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CompanyQueryReq;
import com.test.hangyun.dto.CompanyReq;
import com.test.hangyun.dto.vo.CompanyOptionVO;
import com.test.hangyun.dto.vo.CompanyVO;

import java.util.List;

public interface CompanyService {

    /** 分页查询, 支持按名称/代码前缀模糊搜索 */
    PageResult<CompanyVO> page(CompanyQueryReq req);

    /** 详情 */
    CompanyVO getById(Long id);

    /** 新增 */
    void create(CompanyReq req);

    /** 修改 */
    void update(Long id, CompanyReq req);

    /** 删除, 有集装箱在用这个公司(箱主/操作方)时拒绝 */
    void delete(Long id);

    /**
     * 下拉框候选(给新增集装箱选箱主/操作方用): 最多 20 条, 返回 id/名称/代码。
     * keyword 前缀匹配公司与名称, 不传则返回前 20 条。
     */
    List<CompanyOptionVO> options(String keyword);
}
