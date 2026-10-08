package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.VoyageQueryReq;
import com.test.hangyun.dto.VoyageReq;
import com.test.hangyun.dto.vo.VoyageVO;
import com.test.hangyun.mapper.PortMapper;
import com.test.hangyun.mapper.VoyageMapper;
import com.test.hangyun.pojo.entity.Port;
import com.test.hangyun.pojo.entity.Voyage;
import com.test.hangyun.service.VoyageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 航次管理。
 * <p>
 * 库里**没有** v_voyage 视图, 所以直接读基础表。出参里的两个港口中文名在 port 表上,
 * 由 Service 把当前页用到的港口 id 收集起来**批量**查一次回填。
 * <p>
 * ⚠️ vsl_id 指向 vessel, 但船舶模块还没做(vessel 表是空的), 所以**不校验**它 ——
 * 校验了会挡住所有航次的创建。等第 6 个模块做完再补。
 */
@Service
@RequiredArgsConstructor
public class VoyageServiceImpl implements VoyageService {

    private final VoyageMapper voyageMapper;
    private final PortMapper portMapper;

    @Override
    public PageResult<VoyageVO> page(VoyageQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Voyage> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getNo())) {
            w.likeRight(Voyage::getNo, req.getNo().trim());
        }
        w.eq(req.getLoadingPortId() != null, Voyage::getLoadingPortId, req.getLoadingPortId());
        // 最新录入的排最前(和其他业务实体一致)
        w.orderByDesc(Voyage::getId);

        Page<Voyage> p = voyageMapper.selectPage(new Page<>(pageNo, pageSize), w);

        Map<Long, String> portNames = findPortNames(collectPortIds(p.getRecords()));
        return PageResult.of(p, (Function<Voyage, VoyageVO>) v -> {
            VoyageVO vo = toVO(v, portNames);
            return vo;
        });
    }

    @Override
    public VoyageVO getById(Long id) {
        Voyage v = getExisting(id);
        return toVO(v, findPortNames(collectPortIds(List.of(v))));
    }

    @Override
    @Transactional
    public void create(VoyageReq req) {
        validatePortsExist(req.getLoadingPortId(), req.getDischargePortId());

        Voyage e = new Voyage();
        e.setNo(trimToNull(req.getNo()));
        e.setVslId(req.getVslId());
        e.setLoadingPortId(req.getLoadingPortId());
        e.setDischargePortId(req.getDischargePortId());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        voyageMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, VoyageReq req) {
        getExisting(id);
        validatePortsExist(req.getLoadingPortId(), req.getDischargePortId());

        // 整体覆盖: 每个字段都显式 set(null 也能真正写进去)
        LambdaUpdateWrapper<Voyage> u = new LambdaUpdateWrapper<>();
        u.eq(Voyage::getId, id)
                .set(Voyage::getNo, trimToNull(req.getNo()))
                .set(Voyage::getVslId, req.getVslId())
                .set(Voyage::getLoadingPortId, req.getLoadingPortId())
                .set(Voyage::getDischargePortId, req.getDischargePortId());
        voyageMapper.update(null, u);
    }

    @Override
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

    /** 两个港口都要校验存在(库里没有外键约束; vsl_id 见类注释, 暂不校验) */
    private void validatePortsExist(Long loadingPortId, Long dischargePortId) {
        if (loadingPortId != null && portMapper.selectById(loadingPortId) == null) {
            throw new BizException("起始港口不存在", "loadingPortId=" + loadingPortId);
        }
        if (dischargePortId != null && portMapper.selectById(dischargePortId) == null) {
            throw new BizException("目的港口不存在", "dischargePortId=" + dischargePortId);
        }
    }

    private VoyageVO toVO(Voyage v, Map<Long, String> portNames) {
        VoyageVO vo = new VoyageVO();
        vo.setId(v.getId());
        vo.setNo(v.getNo());
        vo.setVslId(v.getVslId());
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

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
