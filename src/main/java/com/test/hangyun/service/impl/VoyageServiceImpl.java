package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.VoyageQueryReq;
import com.test.hangyun.dto.VoyageReq;
import com.test.hangyun.dto.vo.VoyageVO;
import com.test.hangyun.mapper.PortMapper;
import com.test.hangyun.mapper.VesselMapper;
import com.test.hangyun.mapper.VoyageMapper;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.Port;
import com.test.hangyun.pojo.entity.Vessel;
import com.test.hangyun.pojo.entity.Voyage;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.VoyageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 航次管理。
 * <p>
 * 库里**没有** v_voyage 视图, 所以直接读基础表。出参里的两个港口中文名和船名分别落在
 * port / vessel 表上, 由 Service 把当前页用到的 id 收集起来**批量**查一次回填
 * (两个港口的 id 合成一批查, 船名的 id 另查一批)。
 * <p>
 * 三个引用(vslId / 两个港口)都由应用层校验存在性。
 * <p>
 * ⚠️ 航次号**不单独查重**: 航次号由船公司自编, 跨公司重号是常态。唯一的是
 * **(vsl_id, no) 这一对** —— 同一艘船不能有两个同号航次, 不同船可以重号。
 */
@Service
@RequiredArgsConstructor
public class VoyageServiceImpl implements VoyageService {

    private final VoyageMapper voyageMapper;
    private final PortMapper portMapper;
    private final VesselMapper vesselMapper;

    @Override
    public PageResult<VoyageVO> page(VoyageQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Voyage> w = buildWrapper(req);
        // 最新录入的排最前(和其他业务实体一致)
        w.orderByDesc(Voyage::getId);

        Page<Voyage> p = voyageMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(), assemble(p.getRecords()));
    }

    @Override
    @OpLog(module = "航次管理", table = "voyage", type = OpType.EXPORT, desc = "导出 Excel")
    public List<VoyageVO> listForExport(VoyageQueryReq req) {
        LambdaQueryWrapper<Voyage> w = buildWrapper(req);
        w.orderByDesc(Voyage::getId);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<Voyage> rows = voyageMapper.selectList(w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return assemble(rows);
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<Voyage> buildWrapper(VoyageQueryReq req) {
        LambdaQueryWrapper<Voyage> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getNo())) {
            w.likeRight(Voyage::getNo, req.getNo().trim());
        }
        w.eq(req.getLoadingPortId() != null, Voyage::getLoadingPortId, req.getLoadingPortId());
        return w;
    }

    /** 列表和导出共用的组装: 批量查港口中文名和船名后回填 */
    private List<VoyageVO> assemble(List<Voyage> records) {
        Map<Long, String> portNames = findPortNames(collectPortIds(records));
        Map<Long, String> vesselNames = findVesselNames(collectVesselIds(records));
        return records.stream().map(v -> toVO(v, portNames, vesselNames)).toList();
    }

    @Override
    public VoyageVO getById(Long id) {
        Voyage v = getExisting(id);
        List<Voyage> one = List.of(v);
        return toVO(v, findPortNames(collectPortIds(one)), findVesselNames(collectVesselIds(one)));
    }

    @Override
    @OpLog(module = "航次管理", table = "voyage", type = OpType.INSERT, desc = "新增航次")
    @Transactional
    public void create(VoyageReq req) {
        validateReferences(req.getVslId(), req.getLoadingPortId(), req.getDischargePortId());
        String no = trimToNull(req.getNo());
        ensureVoyageNoUnique(req.getVslId(), no, null);

        Voyage e = new Voyage();
        e.setNo(no);
        e.setVslId(req.getVslId());
        e.setLoadingPortId(req.getLoadingPortId());
        e.setDischargePortId(req.getDischargePortId());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        voyageMapper.insert(e);
    }

    @Override
    @OpLog(module = "航次管理", table = "voyage", type = OpType.UPDATE, desc = "修改航次")
    @Transactional
    public void update(Long id, VoyageReq req) {
        getExisting(id);
        validateReferences(req.getVslId(), req.getLoadingPortId(), req.getDischargePortId());
        String no = trimToNull(req.getNo());
        // 查重时排除自己, 否则"只改港口、船和航次号不变"的提交会被误判为重复
        ensureVoyageNoUnique(req.getVslId(), no, id);

        // 整体覆盖: 每个字段都显式 set(null 也能真正写进去)
        LambdaUpdateWrapper<Voyage> u = new LambdaUpdateWrapper<>();
        u.eq(Voyage::getId, id)
                .set(Voyage::getNo, no)
                .set(Voyage::getVslId, req.getVslId())
                .set(Voyage::getLoadingPortId, req.getLoadingPortId())
                .set(Voyage::getDischargePortId, req.getDischargePortId());
        voyageMapper.update(null, u);
    }

    @Override
    @OpLog(module = "航次管理", table = "voyage", type = OpType.DELETE, desc = "删除航次")
    @Transactional
    public void delete(Long id) {
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被物流事件引用的航次
        long events = voyageMapper.countEventsByVoyageId(id);
        if (events > 0) {
            throw BizException.conflict("该航次下存在 " + events + " 条物流事件, 无法删除");
        }
        voyageMapper.deleteById(id);
    }

    private Voyage getExisting(Long id) {
        Voyage e = voyageMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("航次不存在");
        }
        return e;
    }

    /**
     * (vsl_id, no) 是业务上的复合唯一: 同一艘船的同一航次号只能有一条, 不同船可以重号。
     * 库里也有同样的唯一约束(uq_voyage_vsl_no), 这里提前查是为了返回 409 而不是撞唯一键报 500。
     * <p>
     * 两项里**只要有一个为空就跳过** —— 和库里的唯一约束口径一致: PostgreSQL 的 UNIQUE
     * 不比较 NULL, 所以 (null, '2026E001') 这种行可以有任意多条, 不算冲突。
     */
    private void ensureVoyageNoUnique(Long vslId, String no, Long excludeId) {
        if (vslId == null || no == null) {
            return;
        }
        LambdaQueryWrapper<Voyage> w = new LambdaQueryWrapper<>();
        w.eq(Voyage::getVslId, vslId);
        w.eq(Voyage::getNo, no);
        w.ne(excludeId != null, Voyage::getId, excludeId);
        if (voyageMapper.selectCount(w) > 0) {
            throw BizException.conflict("该船舶下已存在航次号: " + no);
        }
    }

    /** 三个引用都要校验存在(库里没有外键约束) */
    private void validateReferences(Long vslId, Long loadingPortId, Long dischargePortId) {
        if (vslId != null && vesselMapper.selectById(vslId) == null) {
            throw new BizException("船舶不存在", "vslId=" + vslId);
        }
        if (loadingPortId != null && portMapper.selectById(loadingPortId) == null) {
            throw new BizException("起始港口不存在", "loadingPortId=" + loadingPortId);
        }
        if (dischargePortId != null && portMapper.selectById(dischargePortId) == null) {
            throw new BizException("目的港口不存在", "dischargePortId=" + dischargePortId);
        }
    }

    private VoyageVO toVO(Voyage v, Map<Long, String> portNames, Map<Long, String> vesselNames) {
        VoyageVO vo = new VoyageVO();
        vo.setId(v.getId());
        vo.setNo(v.getNo());
        vo.setVslId(v.getVslId());
        vo.setVslName(vesselNames.get(v.getVslId()));
        vo.setLoadingPortId(v.getLoadingPortId());
        vo.setLoadingPortName(portNames.get(v.getLoadingPortId()));
        vo.setDischargePortId(v.getDischargePortId());
        vo.setDischargePortName(portNames.get(v.getDischargePortId()));
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        return vo;
    }

    /** 当前页里出现过的港口 id(去重、去 null), 起运港和目的港一起收集 */
    private List<Long> collectPortIds(List<Voyage> records) {
        return records.stream()
                .flatMap(v -> java.util.stream.Stream.of(v.getLoadingPortId(), v.getDischargePortId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /** 一次查出这批港口的 id -> 中文名。空集合直接返回空表, 不发查询 */
    private Map<Long, String> findPortNames(List<Long> portIds) {
        if (portIds.isEmpty()) {
            return Map.of();
        }
        // 用 HashMap 而不是 Collectors.toMap: toMap 不允许 value 为 null
        Map<Long, String> names = new HashMap<>();
        for (Port p : portMapper.selectBatchIds(portIds)) {
            names.put(p.getId(), p.getCnname());
        }
        return names;
    }

    /** 当前页里出现过的船舶 id(去重、去 null) */
    private List<Long> collectVesselIds(List<Voyage> records) {
        return records.stream()
                .map(Voyage::getVslId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /** 一次查出这批船舶的 id -> 船名。空集合直接返回空表, 不发查询 */
    private Map<Long, String> findVesselNames(List<Long> vslIds) {
        if (vslIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> names = new HashMap<>();
        for (Vessel v : vesselMapper.selectBatchIds(vslIds)) {
            names.put(v.getId(), v.getName());
        }
        return names;
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
