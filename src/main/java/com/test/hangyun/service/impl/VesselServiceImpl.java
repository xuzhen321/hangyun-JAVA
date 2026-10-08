package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.VesselCreateReq;
import com.test.hangyun.dto.VesselQueryReq;
import com.test.hangyun.dto.VesselUpdateReq;
import com.test.hangyun.dto.vo.VesselOptionVO;
import com.test.hangyun.dto.vo.VesselVO;
import com.test.hangyun.mapper.CompanyMapper;
import com.test.hangyun.mapper.CountryMapper;
import com.test.hangyun.mapper.ShipTypeMapper;
import com.test.hangyun.mapper.VesselMapper;
import com.test.hangyun.mapper.VesselViewMapper;
import com.test.hangyun.pojo.entity.Company;
import com.test.hangyun.pojo.entity.Vessel;
import com.test.hangyun.pojo.view.VesselView;
import com.test.hangyun.service.VesselService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 船舶管理。
 * <p>
 * 查询全部走**视图 v_vessel** —— 船旗国、船型、船东、管理公司的名称它都联好了,
 * 连四个 xxx_id 也在视图里(那几列是后补的, 见 viewInitial.sql), 所以**不用 Service 层组装**。
 * 写走表 vessel。
 * <p>
 * mmsi / imo / callsign 三列有唯一约束, 新增/修改都要查重;
 * voyage.vsl_id 引用它, 删除前要查引用。
 */
@Service
@RequiredArgsConstructor
public class VesselServiceImpl implements VesselService {

    private final VesselMapper vesselMapper;
    private final VesselViewMapper vesselViewMapper;
    private final ShipTypeMapper shipTypeMapper;
    private final CountryMapper countryMapper;
    private final CompanyMapper companyMapper;

    @Override
    public PageResult<VesselVO> page(VesselQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<VesselView> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, req.getKeyword());
        w.eq(req.getCountryId() != null, VesselView::getCountryId, req.getCountryId());
        w.eq(req.getVesselTypeId() != null, VesselView::getVesselTypeId, req.getVesselTypeId());
        w.orderByAsc(VesselView::getId);

        Page<VesselView> p = vesselViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, VesselVO::from);
    }

    @Override
    public VesselVO getById(Long id) {
        VesselView v = vesselViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("船舶不存在");
        }
        return VesselVO.from(v);
    }

    @Override
    public List<VesselOptionVO> options(String keyword) {
        // 下拉框只要本表字段(船名/MMSI), 不需要联表, 所以读基表 vessel 而不是视图 v_vessel ——
        // 这是项目里 /xxx/options 的统一约定(见后端接口设计文档 5.2), 客户/港口/公司等都是这么写的。
        // 读基表还有个实际好处: 视图定义过时也不会让下拉框挂掉。
        LambdaQueryWrapper<Vessel> w = new LambdaQueryWrapper<>();
        applyKeywordFilterOnEntity(w, keyword);
        w.orderByAsc(Vessel::getId);

        // searchCount=false: 下拉框不需要 total
        return vesselMapper.selectPage(new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(VesselOptionVO::from).toList();
    }

    @Override
    @Transactional
    public void create(VesselCreateReq req) {
        String mmsi = trimToNull(req.getMmsi());
        String imo = trimToNull(req.getImo());
        String callsign = trimToNull(req.getCallsign());
        ensureUnique(mmsi, imo, callsign, null);
        validateReferences(req.getVesselTypeId(), req.getCountryId(),
                req.getOwnerCompanyId(), req.getManagerCompanyId());

        Vessel e = new Vessel();
        e.setName(req.getName().trim());
        e.setVesselTypeId(req.getVesselTypeId());
        e.setCountryId(req.getCountryId());
        e.setMmsi(mmsi);
        e.setImo(imo);
        e.setBuildYear(req.getBuildYear());
        e.setOwnerCompanyId(req.getOwnerCompanyId());
        e.setManagerCompanyId(req.getManagerCompanyId());
        e.setCallsign(callsign);
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        vesselMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, VesselUpdateReq req) {
        getExisting(id);
        String mmsi = trimToNull(req.getMmsi());
        String imo = trimToNull(req.getImo());
        String callsign = trimToNull(req.getCallsign());
        // 查重时排除自己, 否则"只改船名、MMSI 不变"的提交会被误判为重复
        ensureUnique(mmsi, imo, callsign, id);
        validateReferences(req.getVesselTypeId(), req.getCountryId(),
                req.getOwnerCompanyId(), req.getManagerCompanyId());

        // 整体覆盖: 每个字段都显式 set(null 也能真正写进去)
        LambdaUpdateWrapper<Vessel> u = new LambdaUpdateWrapper<>();
        u.eq(Vessel::getId, id)
                .set(Vessel::getName, req.getName().trim())
                .set(Vessel::getVesselTypeId, req.getVesselTypeId())
                .set(Vessel::getCountryId, req.getCountryId())
                .set(Vessel::getMmsi, mmsi)
                .set(Vessel::getImo, imo)
                .set(Vessel::getBuildYear, req.getBuildYear())
                .set(Vessel::getOwnerCompanyId, req.getOwnerCompanyId())
                .set(Vessel::getManagerCompanyId, req.getManagerCompanyId())
                .set(Vessel::getCallsign, callsign);
        vesselMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被航次引用的船
        long voyages = vesselMapper.countVoyagesByVesselId(id);
        if (voyages > 0) {
            throw BizException.conflict("该船被 " + voyages + " 个航次引用, 无法删除");
        }
        vesselMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }

        // 严格语义: 只要有一艘被航次引用, 整批拒绝, 不做部分删除。
        // 船舶是物理删除, 悄悄留下几艘没删掉的话, 用户会以为全删成功了。
        List<Long> blockedIds = vesselMapper.findReferencedIds(distinctIds);
        if (!blockedIds.isEmpty()) {
            // 提示里列**船名**(比一串 id 好认), 所以要再查一次这几条
            List<String> names = vesselMapper.selectBatchIds(blockedIds).stream()
                    .map(Vessel::getName).toList();
            throw BizException.conflict("以下船舶被航次引用, 无法删除: " + String.join("、", names));
        }
        vesselMapper.delete(new LambdaQueryWrapper<Vessel>().in(Vessel::getId, distinctIds));
    }

    private Vessel getExisting(Long id) {
        Vessel e = vesselMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("船舶不存在");
        }
        return e;
    }

    /**
     * mmsi / imo / callsign 三列都有唯一约束, 分别查重, 以便返回 409 而不是撞唯一键报 500。
     * <p>
     * 三项在 DTO 上都已经是 {@code @NotBlank}(必填), 所以正常请求走不到 null 分支;
     * 这里的判空只是兜底 —— 库里的唯一约束允许多行为 null, 万一真有 null 也不会互相冲突。
     */
    private void ensureUnique(String mmsi, String imo, String callsign, Long excludeId) {
        checkUnique(Vessel::getMmsi, mmsi, excludeId, "MMSI 已存在: ");
        checkUnique(Vessel::getImo, imo, excludeId, "IMO 已存在: ");
        checkUnique(Vessel::getCallsign, callsign, excludeId, "呼号已存在: ");
    }

    private void checkUnique(com.baomidou.mybatisplus.core.toolkit.support.SFunction<Vessel, String> column,
                            String value, Long excludeId, String messagePrefix) {
        if (value == null) {
            return;
        }
        LambdaQueryWrapper<Vessel> w = new LambdaQueryWrapper<>();
        w.eq(column, value);
        w.ne(excludeId != null, Vessel::getId, excludeId);
        if (vesselMapper.selectCount(w) > 0) {
            throw BizException.conflict(messagePrefix + value);
        }
    }

    /** 四个引用的字典记录都必须存在(库里没有外键约束) */
    private void validateReferences(Long vesselTypeId, Long countryId,
                                    Long ownerCompanyId, Long managerCompanyId) {
        if (vesselTypeId != null && shipTypeMapper.selectById(vesselTypeId) == null) {
            throw new BizException("船舶类型不存在", "vesselTypeId=" + vesselTypeId);
        }
        if (countryId != null && countryMapper.selectById(countryId) == null) {
            throw new BizException("船旗国不存在", "countryId=" + countryId);
        }
        checkCompany(ownerCompanyId, "船东", "ownerCompanyId");
        checkCompany(managerCompanyId, "管理公司", "managerCompanyId");
    }

    /**
     * 公司必须存在, **而且已经填了"代码"**。
     * <p>
     * 船舶列表里的「船东代码」「管理公司代码」两列, 取自 company.code(经视图 own.code / mgr.code),
     * 公司没填代码这两列就是空白。代码是给用户认公司用的(名称相近的公司靠它区分), 空着就没意义,
     * 所以选船东/管理公司时就把关。
     * <p>
     * ⚠️ **没有做成"company.code 全局必填"**: company 是箱主 / 操作方 / 船东 / 管理公司共用的字典,
     * 那样会连带改到集装箱模块的契约, 超出船舶这块的范围。要全局强制的话单独说。
     */
    private void checkCompany(Long companyId, String label, String field) {
        if (companyId == null) {
            return;
        }
        Company c = companyMapper.selectById(companyId);
        if (c == null) {
            throw new BizException(label + "不存在", field + "=" + companyId);
        }
        if (!StringUtils.hasText(c.getCode())) {
            throw new BizException(label + "必须填写公司代码", field + "=" + companyId);
        }
    }

    /**
     * 关键字同时匹配**船名**、**MMSI**、**IMO** 的前缀, 任一命中即可。
     * 用 and(...) 把三个 OR 包起来, 否则后面那两个 eq 会被 OR 拆散、绕过过滤。
     */
    private void applyKeywordFilter(LambdaQueryWrapper<VesselView> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(VesselView::getShipname, kw)
                .or().likeRight(VesselView::getMmsi, kw)
                .or().likeRight(VesselView::getImo, kw));
    }

    /** 基表上的同款过滤(下拉框走基表, 基表里这列叫 name 而不是 shipname) */
    private void applyKeywordFilterOnEntity(LambdaQueryWrapper<Vessel> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(Vessel::getName, kw)
                .or().likeRight(Vessel::getMmsi, kw)
                .or().likeRight(Vessel::getImo, kw));
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
