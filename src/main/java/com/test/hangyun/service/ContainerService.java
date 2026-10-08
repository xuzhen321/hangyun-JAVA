package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ContainerCreateReq;
import com.test.hangyun.dto.ContainerQueryReq;
import com.test.hangyun.dto.ContainerUpdateReq;
import com.test.hangyun.dto.vo.ContainerVO;

import java.util.List;

public interface ContainerService {

    /** 分页查询, 支持按箱号前缀、状态精确筛选 */
    PageResult<ContainerVO> page(ContainerQueryReq req);

    /** 详情 */
    ContainerVO getById(String no);

    /** 新增: 箱号由前端提供, 必填且不可重复 */
    void create(ContainerCreateReq req);

    /** 修改: **改不了箱号**(主键) */
    void update(String no, ContainerUpdateReq req);

    /** 删除, 有装箱结果引用该箱号时拒绝 */
    void delete(String no);

    /** 批量删除。严格语义: 只要有一个被引用就整批拒绝; 不存在的箱号忽略 */
    void deleteBatch(List<String> nos);
}
