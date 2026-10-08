package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.TrailerCreateReq;
import com.test.hangyun.dto.TrailerQueryReq;
import com.test.hangyun.dto.TrailerUpdateReq;
import com.test.hangyun.dto.vo.TrailerOptionVO;
import com.test.hangyun.dto.vo.TrailerVO;
import com.test.hangyun.mapper.TrailerMapper;
import com.test.hangyun.pojo.entity.Trailer;
import com.test.hangyun.service.TrailerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 拖车信息维护。
 * <p>
 * 拖车号是业务主键(车牌号), 由前端提供, 不可改; container_trailer_record.track_no 引用它,
 * 所以删除前要查引用。insert_time / update_time 交给触发器。
 */
@Service
@RequiredArgsConstructor
public class TrailerServiceImpl implements TrailerService {

    private final TrailerMapper trailerMapper;

    @Override
    public PageResult<TrailerVO> page(TrailerQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Trailer> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, req.getKeyword());
        w.orderByAsc(Trailer::getNo);

        Page<Trailer> p = trailerMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, TrailerVO::from);
    }

    @Override
    public TrailerVO getById(String no) {
        return TrailerVO.from(getExisting(no));
    }

    @Override
    public List<TrailerOptionVO> options(String keyword) {
        LambdaQueryWrapper<Trailer> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, keyword);
        w.orderByAsc(Trailer::getNo);

        // searchCount=false: 下拉框不需要 total, 省掉那条 COUNT
        return trailerMapper.selectPage(
                        new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(TrailerOptionVO::from).toList();
    }

    @Override
    @Transactional
    public void create(TrailerCreateReq req) {
        String no = req.getNo().trim();
        // 拖车号是主键, 提前查重以便返回 409 而不是撞主键报 500
        if (trailerMapper.selectById(no) != null) {
            throw BizException.conflict("拖车号已存在: " + no);
        }

        Trailer e = new Trailer();
        e.setNo(no);
        e.setPhone(trimToNull(req.getPhone()));
        e.setName(trimToNull(req.getName()));
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        trailerMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(String no, TrailerUpdateReq req) {
        getExisting(no);

        // 显式列出要改的列 —— 结构上碰不到主键 no 和 insert_time / update_time
        LambdaUpdateWrapper<Trailer> u = new LambdaUpdateWrapper<>();
        u.eq(Trailer::getNo, no)
                .set(Trailer::getPhone, trimToNull(req.getPhone()))
                .set(Trailer::getName, trimToNull(req.getName()));
        trailerMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(String no) {
        getExisting(no);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被提空箱记录引用的拖车
        long records = trailerMapper.countRecordsByTrailerNo(no);
        if (records > 0) {
            throw BizException.conflict("该拖车已被 " + records + " 条提空箱记录引用, 无法删除");
        }
        trailerMapper.deleteById(no);
    }

    private Trailer getExisting(String no) {
        Trailer e = trailerMapper.selectById(no);
        if (e == null) {
            throw BizException.notFound("拖车不存在");
        }
        return e;
    }

    /**
     * 关键字同时匹配**拖车号**和**司机姓名**的前缀, 任一命中即可。
     * 用 and(...) 把两个 OR 包起来, 否则将来再加别的条件时会被 OR 拆散、绕过过滤。
     */
    private void applyKeywordFilter(LambdaQueryWrapper<Trailer> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(Trailer::getNo, kw).or().likeRight(Trailer::getName, kw));
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
