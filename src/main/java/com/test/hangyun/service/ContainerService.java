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

    /** 逻辑删除: 把状态改成"已删除"，行还在，历史记录查得到 */
    void delete(String no);

    /** 批量逻辑删除。宽松语义: 存在的改状态、不存在的箱号忽略，幂等 */
    void deleteBatch(List<String> nos);
}
