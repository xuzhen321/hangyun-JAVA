package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ContainerCreateReq;
import com.test.hangyun.dto.ContainerQueryReq;
import com.test.hangyun.dto.ContainerUpdateReq;
import com.test.hangyun.dto.vo.ContainerOptionVO;
import com.test.hangyun.dto.vo.ContainerVO;

import java.util.List;

public interface ContainerService {

    /** 分页查询, 支持按箱号前缀、状态精确筛选 */
    PageResult<ContainerVO> page(ContainerQueryReq req);

    /** 详情 */
    ContainerVO getById(String no);

    /**
     * 下拉框候选(给新增装箱结果选箱子用): 最多 20 条, 返回箱号/状态。
     * keyword 按**箱号前缀**匹配, 不传则返回前 20 条。
     * 已删除的集装箱不会出现在候选里（它们不能再装货）。
     */
    List<ContainerOptionVO> options(String keyword);

    /** 新增: 箱号由前端提供, 必填且不可重复 */
    void create(ContainerCreateReq req);

    /** 修改: **改不了箱号**(主键) */
    void update(String no, ContainerUpdateReq req);

    /** 逻辑删除: 把状态改成"已删除"，行还在，历史记录查得到 */
    void delete(String no);

    /** 批量逻辑删除。宽松语义: 存在的改状态、不存在的箱号忽略，幂等 */
    void deleteBatch(List<String> nos);
}
