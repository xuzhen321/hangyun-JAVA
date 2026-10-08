package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.ContainerTypeQueryReq;
import com.test.hangyun.dto.ContainerTypeReq;
import com.test.hangyun.dto.vo.ContainerTypeOptionVO;
import com.test.hangyun.dto.vo.ContainerTypeVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.ContainerTypeMapper;
import com.test.hangyun.pojo.entity.ContainerType;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.ContainerTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 集装箱箱型字典。
 * <p>
 * 表里只有"类别 + 尺寸"两个字段, 都没有唯一约束(允许出现重复的箱型), 所以**不用查重**;
 * 但 container.type_id 逻辑引用它, 删除前必须查引用。
 * insert_time / update_time 交给触发器, 应用层不赋值。
 */
@Service
@RequiredArgsConstructor
public class ContainerTypeServiceImpl implements ContainerTypeService {

    private final ContainerTypeMapper containerTypeMapper;

    @Override
    public PageResult<ContainerTypeVO> page(ContainerTypeQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<ContainerType> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ContainerType::getId);

        Page<ContainerType> p = containerTypeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, ContainerTypeVO::from);
    }

    @Override
    public ContainerTypeVO getById(Long id) {
        return ContainerTypeVO.from(getExisting(id));
    }

    @Override
    public List<ContainerTypeOptionVO> options() {
        // 字典表条数少, 一次性返回全部, 不设上限(同 /cargo-types/options)
        return containerTypeMapper.selectList(
                        new LambdaQueryWrapper<ContainerType>().orderByAsc(ContainerType::getId))
                .stream().map(ContainerTypeOptionVO::from).toList();
    }

    @OpLog(module = "集装箱箱型", table = "container_type", type = OpType.INSERT, desc = "新增集装箱箱型")
    @Override
    @Transactional
    public void create(ContainerTypeReq req) {
        ContainerType e = new ContainerType();
        e.setType(req.getType().trim());
        e.setSize(trimToNull(req.getSize()));
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        containerTypeMapper.insert(e);
    }

    @OpLog(module = "集装箱箱型", table = "container_type", type = OpType.UPDATE, desc = "修改集装箱箱型")
    @Override
    @Transactional
    public void update(Long id, ContainerTypeReq req) {
        getExisting(id);

        LambdaUpdateWrapper<ContainerType> u = new LambdaUpdateWrapper<>();
        u.eq(ContainerType::getId, id)
                .set(ContainerType::getType, req.getType().trim())
                .set(ContainerType::getSize, trimToNull(req.getSize()));
        containerTypeMapper.update(null, u);
    }

    @OpLog(module = "集装箱箱型", table = "container_type", type = OpType.DELETE, desc = "删除集装箱箱型")
    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被集装箱引用的箱型
        long containers = containerTypeMapper.countContainersByTypeId(id);
        if (containers > 0) {
            throw BizException.conflict("该箱型下存在 " + containers + " 个集装箱, 无法删除");
        }
        containerTypeMapper.deleteById(id);
    }

    private ContainerType getExisting(Long id) {
        ContainerType e = containerTypeMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("箱型不存在");
        }
        return e;
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
