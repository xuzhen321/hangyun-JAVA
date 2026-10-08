package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CountryQueryReq;
import com.test.hangyun.dto.CountryReq;
import com.test.hangyun.dto.vo.CountryOptionVO;
import com.test.hangyun.dto.vo.CountryVO;

import java.util.List;

public interface CountryService {

    /** 分页查询, 支持按国家代码/中英文名前缀搜索 */
    PageResult<CountryVO> page(CountryQueryReq req);

    /** 详情 */
    CountryVO getById(Long id);

    /** 新增 */
    void create(CountryReq req);

    /** 修改 */
    void update(Long id, CountryReq req);

    /** 删除, 有船舶(船旗国)或港口(所在国家)在用这个国家时拒绝 */
    void delete(Long id);

    /** 下拉框候选(给新增船舶选船旗国用): 最多 20 条 */
    List<CountryOptionVO> options(String keyword);
}
