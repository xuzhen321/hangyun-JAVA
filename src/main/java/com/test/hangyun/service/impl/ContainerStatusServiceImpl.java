package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ContainerStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.ContainerStatusQueryReq;
import com.test.hangyun.dto.ContainerStatusReq;
import com.test.hangyun.dto.vo.ContainerStatusOptionVO;
import com.test.hangyun.dto.vo.ContainerStatusVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.ContainerStatusMapper;
import com.test.hangyun.pojo.entity.ContainerStatus;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.ContainerStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 集装箱状态字典。
 * <p>
 * 和客户状态 / 订单状态的区别: 这张表有**两列描述**(中英文), 而且两列在库里**都有唯一约束**,
 * 所以新增/修改时要把两列分别查重。container.status_id 逻辑引用它, 删除前必须查引用。
 * <p>
 * 内置状态只有**一个** —— id=7「已删除」(集装箱逻辑删除的落点), 它不能改也不能删。
 * 其余几条(1-6)都是普通字典项, 只要没被集装箱引用就能随便改、随便删。
 * 这点和客户状态(1-3 整段内置)/订单状态(1-4 整段内置)不一样。
 */
@Service
@RequiredArgsConstructor
public class ContainerStatusServiceImpl implements ContainerStatusService {

    private final ContainerStatusMapper containerStatusMapper;

    @Override
    public PageResult<ContainerStatusVO> page(ContainerStatusQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<ContainerStatus> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ContainerStatus::getId);

        Page<ContainerStatus> p = containerStatusMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, ContainerStatusVO::from);
    }

    @Override
    public ContainerStatusVO getById(Long id) {
        return ContainerStatusVO.from(getExisting(id));
    }

    @Override
    public List<ContainerStatusOptionVO> options() {
        // 字典表条数少, 一次性返回全部, 不设上限(同 /cargo-types/options)
        return containerStatusMapper.selectList(
                        new LambdaQueryWrapper<ContainerStatus>().orderByAsc(ContainerStatus::getId))
                .stream().map(ContainerStatusOptionVO::from).toList();
    }

    @OpLog(module = "集装箱状态", table = "container_status", type = OpType.INSERT, desc = "新增集装箱状态")
    @Override
    @Transactional
    public void create(ContainerStatusReq req) {
        String cn = req.getDescriptionCn().trim();
        String en = trimToNull(req.getDescriptionEn());
        ensureUnique(cn, en, null);

        ContainerStatus e = new ContainerStatus();
        e.setDescriptionCn(cn);
        e.setDescriptionEn(en);
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        containerStatusMapper.insert(e);
    }

    @OpLog(module = "集装箱状态", table = "container_status", type = OpType.UPDATE, desc = "修改集装箱状态")
    @Override
    @Transactional
    public void update(Long id, ContainerStatusReq req) {
        // id=7「已删除」是内置状态, 不允许改描述 —— 集装箱的逻辑删除依赖它
        if (ContainerStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("内置状态(已删除)不允许修改");
        }
        getExisting(id);
        String cn = req.getDescriptionCn().trim();
        String en = trimToNull(req.getDescriptionEn());
        // 查重时排除自己, 否则"只改英文描述、中文不变"的提交会被误判为重复
        ensureUnique(cn, en, id);

        LambdaUpdateWrapper<ContainerStatus> u = new LambdaUpdateWrapper<>();
        u.eq(ContainerStatus::getId, id)
                .set(ContainerStatus::getDescriptionCn, cn)
                .set(ContainerStatus::getDescriptionEn, en);
        containerStatusMapper.update(null, u);
    }

    @OpLog(module = "集装箱状态", table = "container_status", type = OpType.DELETE, desc = "删除集装箱状态")
    @Override
    @Transactional
    public void delete(Long id) {
        // id=7「已删除」是内置状态, 一律不允许删除
        if (ContainerStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("内置状态(已删除)不允许删除");
        }
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被集装箱引用的状态
        long containers = containerStatusMapper.countContainersByStatusId(id);
        if (containers > 0) {
            throw BizException.conflict("该状态下存在 " + containers + " 个集装箱, 无法删除");
        }
        containerStatusMapper.deleteById(id);
    }

    private ContainerStatus getExisting(Long id) {
        ContainerStatus e = containerStatusMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("集装箱状态不存在");
        }
        return e;
    }

    /**
     * 中英文两列都有唯一约束, 分别查重, 以便返回 409 而不是撞唯一键报 500。
     * en 为空时跳过 —— 库里的唯一约束允许多行为 null, 不冲突。
     */
    private void ensureUnique(String cn, String en, Long excludeId) {
        LambdaQueryWrapper<ContainerStatus> w = new LambdaQueryWrapper<>();
        w.eq(ContainerStatus::getDescriptionCn, cn);
        w.ne(excludeId != null, ContainerStatus::getId, excludeId);
        if (containerStatusMapper.selectCount(w) > 0) {
            throw BizException.conflict("状态中文描述已存在: " + cn);
        }

        if (en == null) {
            return;
        }
        LambdaQueryWrapper<ContainerStatus> w2 = new LambdaQueryWrapper<>();
        w2.eq(ContainerStatus::getDescriptionEn, en);
        w2.ne(excludeId != null, ContainerStatus::getId, excludeId);
        if (containerStatusMapper.selectCount(w2) > 0) {
            throw BizException.conflict("状态英文描述已存在: " + en);
        }
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
