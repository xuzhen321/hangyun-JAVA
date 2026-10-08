package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ContainerStatusConstants;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.ContainerCreateReq;
import com.test.hangyun.dto.ContainerQueryReq;
import com.test.hangyun.dto.ContainerUpdateReq;
import com.test.hangyun.dto.vo.ContainerOptionVO;
import com.test.hangyun.dto.vo.ContainerVO;
import com.test.hangyun.mapper.CompanyMapper;
import com.test.hangyun.mapper.ContainerMapper;
import com.test.hangyun.mapper.ContainerStatusMapper;
import com.test.hangyun.mapper.ContainerTypeMapper;
import com.test.hangyun.pojo.entity.Company;
import com.test.hangyun.pojo.entity.Container;
import com.test.hangyun.pojo.entity.ContainerStatus;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.ContainerType;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集装箱管理。
 * <p>
 * 库里**没有** v_container 视图, 所以直接读基础表。
 * 出参里的箱型、箱主、操作方、状态描述分别来自 container_type / company / container_status,
 * 本表只存 id —— 没有视图就没法一次联表查完, 所以由 Service 分步:
 * 先分页查 container, 再把当前页用到的几组 id 分别批量查出名称回填。
 * <p>
 * 库里没有物理外键, 所以各个 xxx_id 的存在性都由应用层校验。
 * 两个标志位(char(1) '0'/'1')在进出接口时转成 Boolean, 前端不用关心底层编码。
 */
@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final ContainerMapper containerMapper;
    private final ContainerTypeMapper containerTypeMapper;
    private final ContainerStatusMapper containerStatusMapper;
    private final CompanyMapper companyMapper;

    @Override
    public PageResult<ContainerVO> page(ContainerQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Container> w = buildWrapper(req);
        w.orderByAsc(Container::getNo);

        Page<Container> p = containerMapper.selectPage(new Page<>(pageNo, pageSize), w);
        // 把当前页用到的箱型/公司/状态一次查出来, 回填到 VO(三组 id 三组查询, 不是每行一次)
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(), assemble(p.getRecords()));
    }

    @Override
    @OpLog(module = "集装箱", table = "container", type = OpType.EXPORT, desc = "导出 Excel")
    public List<ContainerVO> listForExport(ContainerQueryReq req) {
        LambdaQueryWrapper<Container> w = buildWrapper(req);
        w.orderByAsc(Container::getNo);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<Container> rows = containerMapper.selectList(
                w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return assemble(rows);
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<Container> buildWrapper(ContainerQueryReq req) {
        LambdaQueryWrapper<Container> w = new LambdaQueryWrapper<>();
        // 箱号是定长业务编码(如 SEGU9481570), 常用前缀筛出某个箱主的箱子
        if (StringUtils.hasText(req.getNo())) {
            w.likeRight(Container::getNo, req.getNo().trim());
        }
        // 逻辑删除: 已删除的集装箱默认不出现在列表里, 只有显式按 status=7 筛选时才列出来。
        // status_id 允许为 null, 而 "status_id <> 7" 对 null 求值为 null(视为不成立),
        // 会连"没有状态"的集装箱一起漏掉, 所以必须额外放行 null。
        if (req.getStatus() != null) {
            w.eq(Container::getStatusId, req.getStatus());
        } else {
            w.and(q -> q.ne(Container::getStatusId, ContainerStatusConstants.STATUS_DELETED)
                    .or().isNull(Container::getStatusId));
        }

        // 箱主 / 操作方都是 company 表, 但它们是两个独立字段, 所以两个条件各查各的
        w.eq(req.getOwnerId() != null, Container::getOwnerId, req.getOwnerId());
        w.eq(req.getOperatorId() != null, Container::getOperatorId, req.getOperatorId());
        return w;
    }

    /** 列表和导出共用的组装: 批量查箱型/公司/状态后回填 */
    private List<ContainerVO> assemble(List<Container> records) {
        Map<Long, ContainerType> types = findTypes(collect(records, Container::getTypeId));
        Map<Long, String> companies = findCompanyNames(
                collect(records, Container::getOwnerId, Container::getOperatorId));
        Map<Long, String> statuses = findStatusDescriptions(
                collect(records, Container::getStatusId));

        return records.stream().map(c -> {
            ContainerVO vo = ContainerVO.from(c);
            ContainerType t = types.get(c.getTypeId());
            if (t != null) {
                vo.setTypeName(t.getType());
                vo.setTypeSize(t.getSize());
            }
            vo.setOwnerName(companies.get(c.getOwnerId()));
            vo.setOperatorName(companies.get(c.getOperatorId()));
            vo.setStatusDescription(statuses.get(c.getStatusId()));
            return vo;
        }).toList();
    }

    @Override
    public ContainerVO getById(String no) {
        Container c = getExisting(no);
        ContainerVO vo = ContainerVO.from(c);

        ContainerType t = c.getTypeId() == null ? null : containerTypeMapper.selectById(c.getTypeId());
        if (t != null) {
            vo.setTypeName(t.getType());
            vo.setTypeSize(t.getSize());
        }
        if (c.getOwnerId() != null) {
            vo.setOwnerName(findCompanyNames(List.of(c.getOwnerId())).get(c.getOwnerId()));
        }
        if (c.getOperatorId() != null) {
            vo.setOperatorName(findCompanyNames(List.of(c.getOperatorId())).get(c.getOperatorId()));
        }
        if (c.getStatusId() != null) {
            vo.setStatusDescription(
                    findStatusDescriptions(List.of(c.getStatusId())).get(c.getStatusId()));
        }
        return vo;
    }

    @Override
    public List<ContainerOptionVO> options(String keyword) {
        LambdaQueryWrapper<Container> w = new LambdaQueryWrapper<>();
        // 箱号是定长业务编码(如 SEGU9481570), 用户一般输前几位来缩小范围
        if (StringUtils.hasText(keyword)) {
            w.likeRight(Container::getNo, keyword.trim());
        }
        // 已删除的箱子不能再装货, 所以不该出现在候选里(和新增校验的口径保持一致)
        w.and(q -> q.ne(Container::getStatusId, ContainerStatusConstants.STATUS_DELETED)
                .or().isNull(Container::getStatusId));
        w.orderByAsc(Container::getNo);

        // searchCount=false: 下拉框不需要 total, 省掉那条 COUNT
        List<Container> records = containerMapper.selectPage(
                new Page<>(1, OptionConstants.OPTION_LIMIT, false), w).getRecords();

        Map<Long, String> statuses = findStatusDescriptions(collect(records, Container::getStatusId));
        return records.stream()
                .map(c -> ContainerOptionVO.of(c, statuses.get(c.getStatusId())))
                .toList();
    }

    @Override
    @OpLog(module = "集装箱", table = "container", type = OpType.INSERT, desc = "新增集装箱")
    @Transactional
    public void create(ContainerCreateReq req) {
        String no = req.getNo().trim();
        // 箱号是主键, 提前查重以便返回 409 而不是撞主键报 500
        if (containerMapper.selectById(no) != null) {
            throw BizException.conflict("箱号已存在: " + no);
        }
        validateReferences(req.getTypeId(), req.getOwnerId(), req.getOperatorId(), req.getStatusId());

        Container e = new Container();
        e.setNo(no);
        applyFields(e, req.getTypeId(), req.getOwnerId(), req.getOperatorId(), req.getSealNo(),
                req.getStatusId(), req.getDangerFlag(), req.getMaritimeFlag(),
                req.getCarrierOperate(), req.getCtrStatusTerminal());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        containerMapper.insert(e);
    }

    @Override
    @OpLog(module = "集装箱", table = "container", type = OpType.UPDATE, desc = "修改集装箱")
    @Transactional
    public void update(String no, ContainerUpdateReq req) {
        getExisting(no);
        validateReferences(req.getTypeId(), req.getOwnerId(), req.getOperatorId(), req.getStatusId());

        // 用 UpdateWrapper 显式列出要改的列:
        //  1. 后端传的 null 能真正写进去(把字段清空), 不会被 "非空才更新" 策略跳过
        //  2. 结构上保证不会碰到主键 no 和 insert_time / update_time
        LambdaUpdateWrapper<Container> u = new LambdaUpdateWrapper<>();
        u.eq(Container::getNo, no)
                .set(Container::getTypeId, req.getTypeId())
                .set(Container::getOwnerId, req.getOwnerId())
                .set(Container::getOperatorId, req.getOperatorId())
                .set(Container::getSealNo, req.getSealNo())
                .set(Container::getStatusId, req.getStatusId())
                .set(Container::getDangerFlag, toFlag(req.getDangerFlag()))
                .set(Container::getMaritimeFlag, toFlag(req.getMaritimeFlag()))
                .set(Container::getCarrierOperate, req.getCarrierOperate())
                .set(Container::getCtrStatusTerminal, req.getCtrStatusTerminal());
        containerMapper.update(null, u);
    }

    @Override
    @OpLog(module = "集装箱", table = "container", type = OpType.DELETE, desc = "删除集装箱")
    @Transactional
    public void delete(String no) {
        getExisting(no);

        // 逻辑删除: 不删行, 只把状态改成"已删除"。
        // 好处是装箱结果等引用不会悬空(**历史记录还能查到**), 所以这里不需要再做引用检查。
        // 已经是已删除时重复调用也安全: 整行没有实际变化, 触发器不会刷新 update_time。
        LambdaUpdateWrapper<Container> u = new LambdaUpdateWrapper<>();
        u.eq(Container::getNo, no)
                .set(Container::getStatusId, ContainerStatusConstants.STATUS_DELETED);
        containerMapper.update(null, u);
    }

    @Override
    @OpLog(module = "集装箱", table = "container", type = OpType.DELETE, desc = "批量删除集装箱")
    @Transactional
    public void deleteBatch(List<String> nos) {
        // 去重: 前端多选时可能传进重复的箱号
        List<String> distinctNos = nos.stream().distinct().toList();
        if (distinctNos.isEmpty()) {
            return;
        }

        // 逻辑删除, 一条 UPDATE 覆盖整批。
        // 语义是**宽松**的: 库里不存在的箱号匹配不到, 自然被忽略 —— 这正是幂等的来源。
        // 注意这和以前不一样了: 以前是物理删除 + 引用保护, 只要有一个被引用就整批拒绝;
        // 现在行还在, 没有"删不掉"的情况, 所以不需要整批拒绝。
        LambdaUpdateWrapper<Container> u = new LambdaUpdateWrapper<>();
        u.in(Container::getNo, distinctNos)
                .set(Container::getStatusId, ContainerStatusConstants.STATUS_DELETED);
        containerMapper.update(null, u);
    }

    private Container getExisting(String no) {
        Container e = containerMapper.selectById(no);
        if (e == null) {
            throw BizException.notFound("集装箱不存在");
        }
        return e;
    }

    /** 把接口层的 9 个字段装进实体(新增用) */
    private void applyFields(Container e, Long typeId, Long ownerId, Long operatorId, String sealNo,
                            Long statusId, Boolean dangerFlag, Boolean maritimeFlag,
                            String carrierOperate, String ctrStatusTerminal) {
        e.setTypeId(typeId);
        e.setOwnerId(ownerId);
        e.setOperatorId(operatorId);
        e.setSealNo(sealNo);
        e.setStatusId(statusId);
        e.setDangerFlag(toFlag(dangerFlag));
        e.setMaritimeFlag(toFlag(maritimeFlag));
        e.setCarrierOperate(carrierOperate);
        e.setCtrStatusTerminal(ctrStatusTerminal);
    }

    /** true -> '1', false -> '0', null 保持 null(库里这两列允许为空) */
    private static String toFlag(Boolean flag) {
        if (flag == null) {
            return null;
        }
        return flag ? "1" : "0";
    }

    private void validateReferences(Long typeId, Long ownerId, Long operatorId, Long statusId) {
        if (typeId != null && containerTypeMapper.selectById(typeId) == null) {
            throw new BizException("箱型不存在", "typeId=" + typeId);
        }
        if (ownerId != null && companyMapper.selectById(ownerId) == null) {
            throw new BizException("箱主不存在", "ownerId=" + ownerId);
        }
        if (operatorId != null && companyMapper.selectById(operatorId) == null) {
            throw new BizException("操作方不存在", "operatorId=" + operatorId);
        }
        if (statusId != null && containerStatusMapper.selectById(statusId) == null) {
            throw new BizException("集装箱状态不存在", "statusId=" + statusId);
        }
    }

    /** 收集当前页里出现过的某个 id(去重、去 null) */
    @SafeVarargs
    private List<Long> collect(List<Container> records, Function<Container, Long>... getters) {
        List<Long> ids = new ArrayList<>();
        for (Container c : records) {
            for (Function<Container, Long> getter : getters) {
                Long id = getter.apply(c);
                if (id != null && !ids.contains(id)) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private Map<Long, ContainerType> findTypes(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return containerTypeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ContainerType::getId, Function.identity()));
    }

    private Map<Long, String> findCompanyNames(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        // 用 HashMap 而不是 Collectors.toMap: toMap 不允许 value 为 null
        Map<Long, String> names = new HashMap<>();
        for (Company c : companyMapper.selectBatchIds(ids)) {
            names.put(c.getId(), c.getName());
        }
        return names;
    }

    private Map<Long, String> findStatusDescriptions(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> names = new HashMap<>();
        for (ContainerStatus s : containerStatusMapper.selectBatchIds(ids)) {
            names.put(s.getId(), s.getDescriptionCn());
        }
        return names;
    }
}
