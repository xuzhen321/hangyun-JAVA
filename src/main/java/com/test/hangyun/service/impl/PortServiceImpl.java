package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.constant.PortConstants;
import com.test.hangyun.dto.PortCreateReq;
import com.test.hangyun.dto.PortQueryReq;
import com.test.hangyun.dto.PortUpdateReq;
import com.test.hangyun.dto.vo.PortOptionVO;
import com.test.hangyun.dto.vo.PortVO;
import com.test.hangyun.mapper.AreaMapper;
import com.test.hangyun.mapper.CountryMapper;
import com.test.hangyun.mapper.HarborSizeMapper;
import com.test.hangyun.mapper.PortLevelMapper;
import com.test.hangyun.mapper.PortMapper;
import com.test.hangyun.mapper.PortTypeMapper;
import com.test.hangyun.mapper.PortViewMapper;
import com.test.hangyun.mapper.TimezoneMapper;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.Port;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.view.PortView;
import com.test.hangyun.service.PortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 港口管理。
 * <p>
 * 查询全部走**视图 v_port** —— 国家、区域、时区、尺寸、级别、类型、母港的名称它都联好了,
 * 六组 xxx_id 也在视图里(后补的列)。写走表 port, 读下拉框走基表(只要本表字段)。
 * <p>
 * 港口是**逻辑删除**(state = '3'), 行还在, 所以订单/事件/航次的引用不会悬空,
 * 删除前**不需要**查引用。state 不接受前端传入, 由后端维护。
 */
@Service
@RequiredArgsConstructor
public class PortServiceImpl implements PortService {

    private final PortMapper portMapper;
    private final PortViewMapper portViewMapper;
    private final CountryMapper countryMapper;
    private final AreaMapper areaMapper;
    private final TimezoneMapper timezoneMapper;
    private final HarborSizeMapper harborSizeMapper;
    private final PortLevelMapper portLevelMapper;
    private final PortTypeMapper portTypeMapper;

    @Override
    public PageResult<PortVO> page(PortQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<PortView> w = buildWrapper(req);
        w.orderByAsc(PortView::getPortId);

        Page<PortView> p = portViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, PortVO::from);
    }

    @Override
    @OpLog(module = "港口管理", table = "port", type = OpType.EXPORT, desc = "导出 Excel")
    public List<PortVO> listForExport(PortQueryReq req) {
        LambdaQueryWrapper<PortView> w = buildWrapper(req);
        w.orderByAsc(PortView::getPortId);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<PortView> rows = portViewMapper.selectList(
                w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return rows.stream().map(PortVO::from).toList();
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<PortView> buildWrapper(PortQueryReq req) {
        LambdaQueryWrapper<PortView> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, req.getKeyword());
        w.eq(req.getCountryId() != null, PortView::getCountryId, req.getCountryId());
        // 逻辑删除: 已删除的港口不出现在列表里。
        // 必须带上 is null 分支 —— SQL 里 "null <> '3'" 结果是 null(不是 true), 只用 ne
        // 会把 state 为空的行一并滤掉。下拉框那边本来就是这么写的, 两处口径要一致。
        w.and(q -> q.ne(PortView::getState, PortConstants.STATE_DELETED)
                .or().isNull(PortView::getState));
        return w;
    }

    @Override
    public PortVO getById(Long id) {
        PortView v = portViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("港口不存在");
        }
        return PortVO.from(v);
    }

    @Override
    public List<PortOptionVO> options(String keyword) {
        LambdaQueryWrapper<Port> w = new LambdaQueryWrapper<>();
        applyKeywordFilterOnEntity(w, keyword);
        // 和列表口径一致: 已删除的港口不能用作起运港/目的港
        w.and(q -> q.ne(Port::getState, PortConstants.STATE_DELETED).or().isNull(Port::getState));
        w.orderByAsc(Port::getUnlocode);

        // searchCount=false: 下拉框不需要 total
        return portMapper.selectPage(new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(PortOptionVO::from).toList();
    }

    @Override
    @OpLog(module = "港口管理", table = "port", type = OpType.INSERT, desc = "新增港口")
    @Transactional
    public void create(PortCreateReq req) {
        String unlocode = req.getUnlocode().trim();
        ensureUnlocodeUnique(unlocode, null);
        validateReferences(req.getCountryId(), req.getAreaId(), req.getTimezoneId(),
                req.getHarborSizeId(), req.getLevelId(), req.getPortTypeId(), req.getParentPortId(), null);

        Port e = new Port();
        e.setUnlocode(unlocode);
        e.setCnname(trimToNull(req.getCnname()));
        e.setEnname(trimToNull(req.getEnname()));
        e.setCountryId(req.getCountryId());
        e.setAreaId(req.getAreaId());
        e.setTimezoneId(req.getTimezoneId());
        e.setHarborSizeId(req.getHarborSizeId());
        e.setLevelId(req.getLevelId());
        e.setPortTypeId(req.getPortTypeId());
        e.setParentPortId(req.getParentPortId());
        e.setLatitude(req.getLatitude());
        e.setLongitude(req.getLongitude());
        e.setGeom(trimToNull(req.getGeom()));
        e.setProvince(trimToNull(req.getProvince()));
        // 新增一律是"0默认"; 之后由删除接口改成 '3'
        e.setState("0");
        portMapper.insert(e);
    }

    @Override
    @OpLog(module = "港口管理", table = "port", type = OpType.UPDATE, desc = "修改港口")
    @Transactional
    public void update(Long id, PortUpdateReq req) {
        getExisting(id);
        String unlocode = req.getUnlocode().trim();
        // 查重时排除自己
        ensureUnlocodeUnique(unlocode, id);
        // 母港不能指向自己
        validateReferences(req.getCountryId(), req.getAreaId(), req.getTimezoneId(),
                req.getHarborSizeId(), req.getLevelId(), req.getPortTypeId(), req.getParentPortId(), id);

        // 整体覆盖: 每个字段都显式 set。
        // ⚠️ 刻意不 set state —— 它由后端维护, 不能让前端把已删除的港口改回来。
        LambdaUpdateWrapper<Port> u = new LambdaUpdateWrapper<>();
        u.eq(Port::getId, id)
                .set(Port::getUnlocode, unlocode)
                .set(Port::getCnname, trimToNull(req.getCnname()))
                .set(Port::getEnname, trimToNull(req.getEnname()))
                .set(Port::getCountryId, req.getCountryId())
                .set(Port::getAreaId, req.getAreaId())
                .set(Port::getTimezoneId, req.getTimezoneId())
                .set(Port::getHarborSizeId, req.getHarborSizeId())
                .set(Port::getLevelId, req.getLevelId())
                .set(Port::getPortTypeId, req.getPortTypeId())
                .set(Port::getParentPortId, req.getParentPortId())
                .set(Port::getLatitude, req.getLatitude())
                .set(Port::getLongitude, req.getLongitude())
                .set(Port::getGeom, trimToNull(req.getGeom()))
                .set(Port::getProvince, trimToNull(req.getProvince()));
        portMapper.update(null, u);
    }

    @Override
    @OpLog(module = "港口管理", table = "port", type = OpType.DELETE, desc = "删除港口")
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        // 逻辑删除: 不删行, 把 state 改成 '3'(删除)。订单/事件/航次的引用不会悬空, 所以不查引用。
        // 已经是已删除时重复调用也安全: 整行没有实际变化, 触发器不会刷新 update_time。
        LambdaUpdateWrapper<Port> u = new LambdaUpdateWrapper<>();
        u.eq(Port::getId, id).set(Port::getState, PortConstants.STATE_DELETED);
        portMapper.update(null, u);
    }

    private Port getExisting(Long id) {
        Port e = portMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("港口不存在");
        }
        return e;
    }

    /** unlocode 有唯一约束, 提前查重以便返回 409 而不是撞唯一键报 500 */
    private void ensureUnlocodeUnique(String unlocode, Long excludeId) {
        LambdaQueryWrapper<Port> w = new LambdaQueryWrapper<>();
        w.eq(Port::getUnlocode, unlocode);
        w.ne(excludeId != null, Port::getId, excludeId);
        if (portMapper.selectCount(w) > 0) {
            throw BizException.conflict("港口五字码已存在: " + unlocode);
        }
    }

    /**
     * 七组引用的记录都必须存在(库里没有外键约束)。
     * selfId 是"当前正在修改的港口 id" —— 母港不能指向自己。
     */
    private void validateReferences(Long countryId, Long areaId, Long timezoneId,
                                    Long harborSizeId, Long levelId, Long portTypeId,
                                    Long parentPortId, Long selfId) {
        if (countryId != null && countryMapper.selectById(countryId) == null) {
            throw new BizException("国家不存在", "countryId=" + countryId);
        }
        if (areaId != null && areaMapper.selectById(areaId) == null) {
            throw new BizException("区域不存在", "areaId=" + areaId);
        }
        if (timezoneId != null && timezoneMapper.selectById(timezoneId) == null) {
            throw new BizException("时区不存在", "timezoneId=" + timezoneId);
        }
        if (harborSizeId != null && harborSizeMapper.selectById(harborSizeId) == null) {
            throw new BizException("港口尺寸不存在", "harborSizeId=" + harborSizeId);
        }
        if (levelId != null && portLevelMapper.selectById(levelId) == null) {
            throw new BizException("港口级别不存在", "levelId=" + levelId);
        }
        if (portTypeId != null && portTypeMapper.selectById(portTypeId) == null) {
            throw new BizException("港口类型不存在", "portTypeId=" + portTypeId);
        }
        if (parentPortId != null) {
            if (parentPortId.equals(selfId)) {
                throw new BizException("母港不能指向自己");
            }
            if (portMapper.selectById(parentPortId) == null) {
                throw new BizException("母港不存在", "parentPortId=" + parentPortId);
            }
        }
    }

    /** 视图上的关键字过滤: 五字码 / 中文名 / 英文名 前缀, 任一命中 */
    private void applyKeywordFilter(LambdaQueryWrapper<PortView> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(PortView::getPortUnlocode, kw)
                .or().likeRight(PortView::getPortCnname, kw)
                .or().likeRight(PortView::getPortEnname, kw));
    }

    /** 基表上的同款过滤(下拉框走基表) */
    private void applyKeywordFilterOnEntity(LambdaQueryWrapper<Port> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(Port::getUnlocode, kw)
                .or().likeRight(Port::getCnname, kw)
                .or().likeRight(Port::getEnname, kw));
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
