package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.EventStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.EventStatusQueryReq;
import com.test.hangyun.dto.EventStatusReq;
import com.test.hangyun.dto.vo.EventStatusOptionVO;
import com.test.hangyun.dto.vo.EventStatusVO;
import com.test.hangyun.mapper.EventStatusMapper;
import com.test.hangyun.pojo.entity.EventStatus;
import com.test.hangyun.service.EventStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 集装箱事件状态字典。
 * <p>
 * 结构和管理方式与集装箱状态(ContainerStatusServiceImpl)完全对称:
 * 中英文两列都有唯一约束, 所以两个都要查重; 删除前要查引用;
 * 内置状态**只有 id=6「已删除」**(事件逻辑删除的落点), 其余 1-5 都能改能删。
 */
@Service
@RequiredArgsConstructor
public class EventStatusServiceImpl implements EventStatusService {

    private final EventStatusMapper eventStatusMapper;

    @Override
    public PageResult<EventStatusVO> page(EventStatusQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<EventStatus> w = new LambdaQueryWrapper<>();
        w.orderByAsc(EventStatus::getId);

        Page<EventStatus> p = eventStatusMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, EventStatusVO::from);
    }

    @Override
    public EventStatusVO getById(Long id) {
        return EventStatusVO.from(getExisting(id));
    }

    @Override
    public List<EventStatusOptionVO> options() {
        // 字典表条数少, 一次性返回全部, 不设上限
        return eventStatusMapper.selectList(
                        new LambdaQueryWrapper<EventStatus>().orderByAsc(EventStatus::getId))
                .stream().map(EventStatusOptionVO::from).toList();
    }

    @Override
    @Transactional
    public void create(EventStatusReq req) {
        String cn = req.getDescriptionCn().trim();
        String en = trimToNull(req.getDescriptionEn());
        ensureUnique(cn, en, null);

        EventStatus e = new EventStatus();
        e.setDescriptionCn(cn);
        e.setDescriptionEn(en);
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        eventStatusMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, EventStatusReq req) {
        // id=6「已删除」是内置状态, 不允许改描述 —— 事件的逻辑删除依赖它
        if (EventStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("内置状态(已删除)不允许修改");
        }
        getExisting(id);
        String cn = req.getDescriptionCn().trim();
        String en = trimToNull(req.getDescriptionEn());
        // 查重时排除自己, 否则"只改英文描述、中文不变"的提交会被误判为重复
        ensureUnique(cn, en, id);

        LambdaUpdateWrapper<EventStatus> u = new LambdaUpdateWrapper<>();
        u.eq(EventStatus::getId, id)
                .set(EventStatus::getDescriptionCn, cn)
                .set(EventStatus::getDescriptionEn, en);
        eventStatusMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // id=6「已删除」是内置状态, 一律不允许删除
        if (EventStatusConstants.isBuiltin(id)) {
            throw BizException.conflict("内置状态(已删除)不允许删除");
        }
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被事件引用的状态
        long events = eventStatusMapper.countEventsByStatusId(id);
        if (events > 0) {
            throw BizException.conflict("该状态下存在 " + events + " 条物流事件, 无法删除");
        }
        eventStatusMapper.deleteById(id);
    }

    private EventStatus getExisting(Long id) {
        EventStatus e = eventStatusMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("事件状态不存在");
        }
        return e;
    }

    /**
     * 中英文两列都有唯一约束, 分别查重, 以便返回 409 而不是撞唯一键报 500。
     * en 为空时跳过 —— 库里的唯一约束允许多行为 null, 不冲突。
     */
    private void ensureUnique(String cn, String en, Long excludeId) {
        LambdaQueryWrapper<EventStatus> w = new LambdaQueryWrapper<>();
        w.eq(EventStatus::getDescriptionCn, cn);
        w.ne(excludeId != null, EventStatus::getId, excludeId);
        if (eventStatusMapper.selectCount(w) > 0) {
            throw BizException.conflict("状态中文描述已存在: " + cn);
        }

        if (en == null) {
            return;
        }
        LambdaQueryWrapper<EventStatus> w2 = new LambdaQueryWrapper<>();
        w2.eq(EventStatus::getDescriptionEn, en);
        w2.ne(excludeId != null, EventStatus::getId, excludeId);
        if (eventStatusMapper.selectCount(w2) > 0) {
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
