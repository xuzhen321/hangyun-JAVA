package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.OperationTypeQueryReq;
import com.test.hangyun.dto.OperationTypeReq;
import com.test.hangyun.dto.vo.OperationTypeOptionVO;
import com.test.hangyun.dto.vo.OperationTypeVO;

import java.util.List;

/**
 * 操作类型字典。**只提供查询和修改**。
 * <p>
 * ⚠️ **没有新增、没有删除** —— 这张表是固定的字典, 五行就是全部(见
 * {@link com.test.hangyun.pojo.enums.OpType})。理由见实现类的类注释。
 * <p>
 * 数据由 {@code operation-type-data.sql} 初始化。
 */
public interface OperationTypeService {

    /** 分页查询, 支持按名称前缀搜索 */
    PageResult<OperationTypeVO> page(OperationTypeQueryReq req);

    /** 详情 */
    OperationTypeVO getById(Long id);

    /** 修改(改的是展示文字, 不动 id) */
    void update(Long id, OperationTypeReq req);

    /** 下拉框候选(一次性返回全部) */
    List<OperationTypeOptionVO> options();
}
