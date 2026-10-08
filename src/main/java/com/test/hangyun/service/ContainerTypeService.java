package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.ContainerTypeQueryReq;
import com.test.hangyun.dto.ContainerTypeReq;
import com.test.hangyun.dto.vo.ContainerTypeOptionVO;
import com.test.hangyun.dto.vo.ContainerTypeVO;

import java.util.List;

public interface ContainerTypeService {

    /** 分页查询 */
    PageResult<ContainerTypeVO> page(ContainerTypeQueryReq req);

    /** 详情 */
    ContainerTypeVO getById(Long id);

    /** 新增 */
    void create(ContainerTypeReq req);

    /** 修改 */
    void update(Long id, ContainerTypeReq req);

    /** 删除, 有集装箱在用这个箱型时拒绝 */
    void delete(Long id);

    /** 下拉框候选: 一次性返回全部(字典表条数少) */
    List<ContainerTypeOptionVO> options();
}
