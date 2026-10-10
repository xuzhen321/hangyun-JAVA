package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ContainerStatusConstants;
import com.test.hangyun.constant.EventStatusConstants;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.ContainerEventQueryReq;
import com.test.hangyun.dto.ContainerEventReq;
import com.test.hangyun.dto.vo.ContainerEventVO;
import com.test.hangyun.mapper.ContainerEventMapper;
import com.test.hangyun.mapper.ContainerEventViewMapper;
import com.test.hangyun.mapper.ContainerMapper;
import com.test.hangyun.mapper.EventStatusMapper;
import com.test.hangyun.mapper.PortMapper;
import com.test.hangyun.mapper.VoyageMapper;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.Container;
import com.test.hangyun.pojo.entity.ContainerEvent;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.view.ContainerEventView;
import com.test.hangyun.service.ContainerEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 集装箱物流事件。
 * <p>
 * 查询全部走**视图 v_container_event** —— 船名、航次号、发生地港口、状态中英文、时区
 * 它都联好了, 所以这个模块**不需要 Service 层二次组装**(比货物/集装箱那几个模块省事)。
 * 写走表 container_event。
 * <p>
 * 库里没有物理外键, 所以箱号/航次/发生地/状态的存在性都由应用层校验。
 * 删除是**逻辑删除**(状态改成"已删除"), 这样物流轨迹还查得到。
 */
@Service
@RequiredArgsConstructor
public class ContainerEventServiceImpl implements ContainerEventService {

    private final ContainerEventMapper containerEventMapper;
    private final ContainerEventViewMapper containerEventViewMapper;
    private final ContainerMapper containerMapper;
    private final VoyageMapper voyageMapper;
    private final PortMapper portMapper;
    private final EventStatusMapper eventStatusMapper;

    @Override
    public PageResult<ContainerEventVO> page(ContainerEventQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<ContainerEventView> w = buildWrapper(req);
        // 列表里最近发生的排最前
        w.orderByDesc(ContainerEventView::getEventTime);

        Page<ContainerEventView> p =
                containerEventViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, ContainerEventVO::from);
    }

    @Override
    @OpLog(module = "物流事件", table = "container_event", type = OpType.EXPORT, desc = "导出 Excel")
    public List<ContainerEventVO> listForExport(ContainerEventQueryReq req) {
        LambdaQueryWrapper<ContainerEventView> w = buildWrapper(req);
        w.orderByDesc(ContainerEventView::getEventTime);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<ContainerEventView> rows = containerEventViewMapper.selectList(
                w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return rows.stream().map(ContainerEventVO::from).toList();
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<ContainerEventView> buildWrapper(ContainerEventQueryReq req) {
        LambdaQueryWrapper<ContainerEventView> w = new LambdaQueryWrapper<>();

        // 箱号是标识符, 用精确匹配(也正好走 container_event 上已有的外键索引)。
        //
        // ⚠️ 必须先把值算好再传, 不能写成 eq(条件, 列, req.getContainerNo().trim()) ——
        // MP 这个"带条件的 eq"里, 条件只控制**要不要拼进 SQL**, 管不了 value 的求值:
        // Java 参数是急切求值的, 三个实参在进 eq 之前就都算完了。
        // 所以不传箱号时那个 .trim() 会对 null 动手, 直接 NPE 500。
        String containerNo = StringUtils.hasText(req.getContainerNo())
                ? req.getContainerNo().trim() : null;
        w.eq(containerNo != null, ContainerEventView::getContainerNo, containerNo);
        w.eq(req.getEventStatusId() != null,
                ContainerEventView::getEventStatusId, req.getEventStatusId());
        // 发生时间闭区间, 单边不传就只限一边
        w.ge(req.getEventTimeFrom() != null,
                ContainerEventView::getEventTime, req.getEventTimeFrom());
        w.le(req.getEventTimeTo() != null,
                ContainerEventView::getEventTime, req.getEventTimeTo());

        excludeDeleted(w);
        return w;
    }

    @Override
    public ContainerEventVO getById(Long id) {
        ContainerEventView v = containerEventViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("物流事件不存在");
        }
        return ContainerEventVO.from(v);
    }

    @Override
    public List<ContainerEventVO> track(String containerNo) {
        // 箱号不存在时给 404, 而不是返回空数组 ——
        // 否则前端分不清"这个箱确实没有事件"和"箱号传错了"。
        // 注意已删除的集装箱**仍然可以查轨迹**(历史记录要留着), 所以这里只判存在、不判删除。
        if (containerMapper.selectById(containerNo) == null) {
            throw BizException.notFound("集装箱不存在");
        }

        LambdaQueryWrapper<ContainerEventView> w = new LambdaQueryWrapper<>();
        w.eq(ContainerEventView::getContainerNo, containerNo);
        excludeDeleted(w);
        // 轨迹按发生时间**升序** —— 前端可以直接拿来画时间轴
        w.orderByAsc(ContainerEventView::getEventTime);

        return containerEventViewMapper.selectList(w).stream()
                .map(ContainerEventVO::from).toList();
    }

    @Override
    public ContainerEventVO latestEvent(String containerNo) {
        LambdaQueryWrapper<ContainerEventView> w = new LambdaQueryWrapper<>();
        w.eq(ContainerEventView::getContainerNo, containerNo);
        excludeDeleted(w);
        w.orderByDesc(ContainerEventView::getEventTime);
        w.last("limit 1");   // 只要最近一条, 不用把整条轨迹都查出来

        List<ContainerEventView> list = containerEventViewMapper.selectList(w);
        return list.isEmpty() ? null : ContainerEventVO.from(list.get(0));
    }

    @Override
    @OpLog(module = "物流事件", table = "container_event", type = OpType.INSERT, desc = "新增物流事件")
    @Transactional
    public void create(ContainerEventReq req) {
        String containerNo = req.getContainerNo().trim();
        validateContainerUsable(containerNo);
        validateVoyageExists(req.getVoyageId());
        validatePlaceExists(req.getEventPlaceId());
        validateStatusExists(req.getEventStatusId());

        ContainerEvent e = new ContainerEvent();
        e.setContainerNo(containerNo);
        e.setSource(req.getSource());
        e.setVoyageId(req.getVoyageId());
        e.setEventPlaceId(req.getEventPlaceId());
        e.setEventStatusId(req.getEventStatusId());
        e.setIsEsti(req.getIsEsti());
        e.setEventTime(req.getEventTime());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        containerEventMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, ContainerEventReq req) {
        getExisting(id);
        String containerNo = req.getContainerNo().trim();
        validateContainerUsable(containerNo);
        validateVoyageExists(req.getVoyageId());
        validatePlaceExists(req.getEventPlaceId());
        validateStatusExists(req.getEventStatusId());

        // 整体覆盖: 每个字段都显式 set(null 也能真正写进去)
        LambdaUpdateWrapper<ContainerEvent> u = new LambdaUpdateWrapper<>();
        u.eq(ContainerEvent::getId, id)
                .set(ContainerEvent::getContainerNo, containerNo)
                .set(ContainerEvent::getSource, req.getSource())
                .set(ContainerEvent::getVoyageId, req.getVoyageId())
                .set(ContainerEvent::getEventPlaceId, req.getEventPlaceId())
                .set(ContainerEvent::getEventStatusId, req.getEventStatusId())
                .set(ContainerEvent::getIsEsti, req.getIsEsti())
                .set(ContainerEvent::getEventTime, req.getEventTime());
        containerEventMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        // 逻辑删除: 不删行, 只把状态改成"已删除"。这样物流轨迹还查得到, 引用也不会悬空。
        // 已经是已删除时重复调用也安全: 整行没有实际变化, 触发器不会刷新 update_time。
        LambdaUpdateWrapper<ContainerEvent> u = new LambdaUpdateWrapper<>();
        u.eq(ContainerEvent::getId, id)
                .set(ContainerEvent::getEventStatusId, EventStatusConstants.STATUS_DELETED);
        containerEventMapper.update(null, u);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        // 逻辑删除, 一条 UPDATE 覆盖整批; 不存在的 id 匹配不到, 自然被忽略(幂等)
        LambdaUpdateWrapper<ContainerEvent> u = new LambdaUpdateWrapper<>();
        u.in(ContainerEvent::getId, distinctIds)
                .set(ContainerEvent::getEventStatusId, EventStatusConstants.STATUS_DELETED);
        containerEventMapper.update(null, u);
    }

    /** 已删除的事件不参与任何查询 —— 注意视图里的状态列叫 eventStatusId */
    private void excludeDeleted(LambdaQueryWrapper<ContainerEventView> w) {
        w.and(q -> q.ne(ContainerEventView::getEventStatusId, EventStatusConstants.STATUS_DELETED)
                .or().isNull(ContainerEventView::getEventStatusId));
    }

    private ContainerEvent getExisting(Long id) {
        ContainerEvent e = containerEventMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("物流事件不存在");
        }
        return e;
    }

    /** 箱号必须存在, 且集装箱**未删除**（已删除的箱子不再产生新事件） */
    private void validateContainerUsable(String containerNo) {
        Container container = containerMapper.selectById(containerNo);
        if (container == null) {
            throw new BizException("集装箱不存在", "containerNo=" + containerNo);
        }
        if (ContainerStatusConstants.isDeleted(container.getStatusId())) {
            throw new BizException("集装箱已删除, 不能登记物流事件: " + containerNo);
        }
    }

    private void validateVoyageExists(Long voyageId) {
        if (voyageId == null) {
            return;
        }
        if (voyageMapper.selectById(voyageId) == null) {
            throw new BizException("航次不存在", "voyageId=" + voyageId);
        }
    }

    private void validatePlaceExists(Long eventPlaceId) {
        if (eventPlaceId == null) {
            return;
        }
        if (portMapper.selectById(eventPlaceId) == null) {
            throw new BizException("发生地港口不存在", "eventPlaceId=" + eventPlaceId);
        }
    }

    private void validateStatusExists(Long eventStatusId) {
        if (eventStatusId == null) {
            return;
        }
        if (eventStatusMapper.selectById(eventStatusId) == null) {
            throw new BizException("事件状态不存在", "eventStatusId=" + eventStatusId);
        }
    }
}
