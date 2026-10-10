package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.OperationTypeQueryReq;
import com.test.hangyun.dto.OperationTypeReq;
import com.test.hangyun.dto.vo.OperationTypeOptionVO;
import com.test.hangyun.dto.vo.OperationTypeVO;
import com.test.hangyun.mapper.OperationTypeMapper;
import com.test.hangyun.pojo.entity.OperationType;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.OperationTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作类型字典维护。
 * <p>
 * ⚠️ **没有新增和删除** —— 这张表是**固定的字典**, 五行就是全部(见 {@link OpType}):
 * <ul>
 *   <li>删一行: 代码里的 {@code @OpLog} 仍按那个 id 写日志, 新日志 JOIN 不出类型名,
 *       操作日志列表的"操作类型"列会变空白 —— 而且不会有任何报错提示你</li>
 *   <li>加一行: 业务代码依赖一份随时可变的字典数据, 而且没有任何 @OpLog 会用到它</li>
 * </ul>
 * 只留**查询 + 修改**(改的是展示用的文字, 不影响 id 与代码的对应关系)。
 * <p>
 * 数据由 {@code operation-type-data.sql} 初始化。
 */
@Service
@RequiredArgsConstructor
public class OperationTypeServiceImpl implements OperationTypeService {

    private final OperationTypeMapper operationTypeMapper;

    @Override
    public PageResult<OperationTypeVO> page(OperationTypeQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<OperationType> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            w.likeRight(OperationType::getType, req.getKeyword().trim());
        }
        w.orderByAsc(OperationType::getId);

        Page<OperationType> p = operationTypeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, OperationTypeVO::from);
    }

    @Override
    public OperationTypeVO getById(Long id) {
        return OperationTypeVO.from(getExisting(id));
    }

    @Override
    public List<OperationTypeOptionVO> options() {
        // 字典表条数少, 一次性返回全部, 不设上限
        return operationTypeMapper.selectList(
                        new LambdaQueryWrapper<OperationType>().orderByAsc(OperationType::getId))
                .stream().map(OperationTypeOptionVO::from).toList();
    }

    @Override
    @Transactional
    public void update(Long id, OperationTypeReq req) {
        getExisting(id);
        String type = req.getType().trim();
        // type 有唯一约束, 查重时排除自己
        ensureTypeUnique(type, id);

        // 整体覆盖。⚠️ 刻意不 set id —— id 是代码与这张表的唯一纽带(OpType 按 id 引用),
        // 改了 id 就等于把日志里的历史记录指向了别的类型
        LambdaUpdateWrapper<OperationType> u = new LambdaUpdateWrapper<>();
        u.eq(OperationType::getId, id).set(OperationType::getType, type);
        operationTypeMapper.update(null, u);
    }

    private OperationType getExisting(Long id) {
        OperationType e = operationTypeMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("操作类型不存在");
        }
        return e;
    }

    /** type 有唯一约束, 提前查重以便返回 409 而不是撞唯一键报 500 */
    private void ensureTypeUnique(String type, Long excludeId) {
        LambdaQueryWrapper<OperationType> w = new LambdaQueryWrapper<>();
        w.eq(OperationType::getType, type);
        w.ne(excludeId != null, OperationType::getId, excludeId);
        if (operationTypeMapper.selectCount(w) > 0) {
            throw BizException.conflict("操作类型已存在: " + type);
        }
    }
}
