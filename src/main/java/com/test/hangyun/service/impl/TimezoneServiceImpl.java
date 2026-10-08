package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.TimezoneQueryReq;
import com.test.hangyun.dto.TimezoneReq;
import com.test.hangyun.dto.vo.TimezoneOptionVO;
import com.test.hangyun.dto.vo.TimezoneVO;
import com.test.hangyun.mapper.TimezoneMapper;
import com.test.hangyun.pojo.entity.Timezone;
import com.test.hangyun.service.TimezoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 时区字典。两列都有唯一约束, 都要查重; 删除前查引用(港口)。 */
@Service
@RequiredArgsConstructor
public class TimezoneServiceImpl implements TimezoneService {

    private final TimezoneMapper timezoneMapper;

    @Override
    public PageResult<TimezoneVO> page(TimezoneQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Timezone> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            // 两个时区字段, 任一前缀命中即可
            w.and(q -> q.likeRight(Timezone::getTimezoneUtc8, kw)
                    .or().likeRight(Timezone::getTimezoneAsiaShanghai, kw));
        }
        w.orderByAsc(Timezone::getId);

        Page<Timezone> p = timezoneMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, TimezoneVO::from);
    }

    @Override
    public TimezoneVO getById(Long id) {
        return TimezoneVO.from(getExisting(id));
    }

    @Override
    public List<TimezoneOptionVO> options() {
        return timezoneMapper.selectList(
                        new LambdaQueryWrapper<Timezone>().orderByAsc(Timezone::getId))
                .stream().map(TimezoneOptionVO::from).toList();
    }

    @Override
    @Transactional
    public void create(TimezoneReq req) {
        String utc8 = req.getTimezoneUtc8().trim();
        String asia = req.getTimezoneAsiaShanghai().trim();
        ensureUnique(utc8, asia, null);

        Timezone e = new Timezone();
        e.setTimezoneUtc8(utc8);
        e.setTimezoneAsiaShanghai(asia);
        timezoneMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, TimezoneReq req) {
        getExisting(id);
        String utc8 = req.getTimezoneUtc8().trim();
        String asia = req.getTimezoneAsiaShanghai().trim();
        // 查重时排除自己
        ensureUnique(utc8, asia, id);

        LambdaUpdateWrapper<Timezone> u = new LambdaUpdateWrapper<>();
        u.eq(Timezone::getId, id)
                .set(Timezone::getTimezoneUtc8, utc8)
                .set(Timezone::getTimezoneAsiaShanghai, asia);
        timezoneMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        long ports = timezoneMapper.countPortsByTimezoneId(id);
        if (ports > 0) {
            throw BizException.conflict("该时区下有 " + ports + " 个港口, 无法删除");
        }
        timezoneMapper.deleteById(id);
    }

    private Timezone getExisting(Long id) {
        Timezone e = timezoneMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("时区不存在");
        }
        return e;
    }

    /** 两列都有唯一约束, 分别查重, 以便返回 409 而不是撞唯一键报 500 */
    private void ensureUnique(String utc8, String asia, Long excludeId) {
        LambdaQueryWrapper<Timezone> w1 = new LambdaQueryWrapper<>();
        w1.eq(Timezone::getTimezoneUtc8, utc8);
        w1.ne(excludeId != null, Timezone::getId, excludeId);
        if (timezoneMapper.selectCount(w1) > 0) {
            throw BizException.conflict("港口时区已存在: " + utc8);
        }

        LambdaQueryWrapper<Timezone> w2 = new LambdaQueryWrapper<>();
        w2.eq(Timezone::getTimezoneAsiaShanghai, asia);
        w2.ne(excludeId != null, Timezone::getId, excludeId);
        if (timezoneMapper.selectCount(w2) > 0) {
            throw BizException.conflict("港口时区(+8)已存在: " + asia);
        }
    }
}
