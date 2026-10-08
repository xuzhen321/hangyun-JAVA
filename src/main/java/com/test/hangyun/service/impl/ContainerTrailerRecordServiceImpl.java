package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ContainerStatusConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.ContainerTrailerRecordQueryReq;
import com.test.hangyun.dto.ContainerTrailerRecordReq;
import com.test.hangyun.dto.vo.ContainerTrailerRecordVO;
import com.test.hangyun.mapper.ContainerMapper;
import com.test.hangyun.mapper.ContainerTrailerRecordMapper;
import com.test.hangyun.mapper.TrailerMapper;
import com.test.hangyun.pojo.entity.Container;
import com.test.hangyun.pojo.entity.ContainerTrailerRecord;
import com.test.hangyun.pojo.entity.Trailer;
import com.test.hangyun.service.ContainerTrailerRecordService;
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
 * 提空箱登记（集装箱拖车记录）。
 * <p>
 * 库里**没有**这张表的视图, 所以直接读基础表。出参里的"司机姓名"在 trailer 表上,
 * 本表只有拖车号 —— 由 Service 把当前页用到的拖车号收集起来**批量**查一次回填。
 * <p>
 * 库里没有物理外键, 所以箱号和拖车号的存在性都由应用层校验。
 * 这张表是链路末端(没有别的表引用它), 物理删除即可。
 */
@Service
@RequiredArgsConstructor
public class ContainerTrailerRecordServiceImpl implements ContainerTrailerRecordService {

    private final ContainerTrailerRecordMapper recordMapper;
    private final ContainerMapper containerMapper;
    private final TrailerMapper trailerMapper;

    @Override
    public PageResult<ContainerTrailerRecordVO> page(ContainerTrailerRecordQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<ContainerTrailerRecord> w = new LambdaQueryWrapper<>();
        // 箱号和拖车号都是用户会输前几位的业务编码, 用前缀匹配
        if (StringUtils.hasText(req.getContainerNo())) {
            w.likeRight(ContainerTrailerRecord::getContainerNo, req.getContainerNo().trim());
        }
        if (StringUtils.hasText(req.getTrackNo())) {
            w.likeRight(ContainerTrailerRecord::getTrackNo, req.getTrackNo().trim());
        }
        // 进场时间区间, 两端都是闭区间, 单边不传就只限一边
        w.ge(req.getDcInDateFrom() != null,
                ContainerTrailerRecord::getDcInDate, req.getDcInDateFrom());
        w.le(req.getDcInDateTo() != null,
                ContainerTrailerRecord::getDcInDate, req.getDcInDateTo());
        w.orderByDesc(ContainerTrailerRecord::getDcInDate);

        Page<ContainerTrailerRecord> p = recordMapper.selectPage(new Page<>(pageNo, pageSize), w);

        // 回填司机姓名: 收集当前页的拖车号, 一次查出来(不是每行查一次)
        Map<String, String> driverNames = findDriverNames(collectTrackNos(p.getRecords()));
        return PageResult.of(p, (Function<ContainerTrailerRecord, ContainerTrailerRecordVO>)
                r -> ContainerTrailerRecordVO.from(r, driverNames.get(r.getTrackNo())));
    }

    @Override
    public ContainerTrailerRecordVO getById(Long id) {
        ContainerTrailerRecord e = getExisting(id);
        return ContainerTrailerRecordVO.from(e,
                findDriverNames(List.of(e.getTrackNo())).get(e.getTrackNo()));
    }

    @Override
    @Transactional
    public void create(ContainerTrailerRecordReq req) {
        String containerNo = req.getContainerNo().trim();
        String trackNo = req.getTrackNo().trim();
        validateContainerUsable(containerNo);
        validateTrailerExists(trackNo);

        ContainerTrailerRecord e = new ContainerTrailerRecord();
        e.setContainerNo(containerNo);
        e.setTrackNo(trackNo);
        e.setDcInDate(req.getDcInDate());
        e.setDcOutDate(req.getDcOutDate());
        e.setUseNote(req.getUseNote());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        recordMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, ContainerTrailerRecordReq req) {
        getExisting(id);
        String containerNo = req.getContainerNo().trim();
        String trackNo = req.getTrackNo().trim();
        validateContainerUsable(containerNo);
        validateTrailerExists(trackNo);

        // 整体覆盖: 每个字段都显式 set(null 也能真正写进去),
        // 结构上也保证碰不到主键和 insert_time / update_time。
        LambdaUpdateWrapper<ContainerTrailerRecord> u = new LambdaUpdateWrapper<>();
        u.eq(ContainerTrailerRecord::getId, id)
                .set(ContainerTrailerRecord::getContainerNo, containerNo)
                .set(ContainerTrailerRecord::getTrackNo, trackNo)
                .set(ContainerTrailerRecord::getDcInDate, req.getDcInDate())
                .set(ContainerTrailerRecord::getDcOutDate, req.getDcOutDate())
                .set(ContainerTrailerRecord::getUseNote, req.getUseNote());
        recordMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        // 物理删除。这张表是链路末端(没有别的表引用它), 所以不需要引用检查。
        recordMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 去重: 前端多选时可能传进重复的 id
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        // 没有引用保护, 所以不存在"删不掉"的情况: 存在的删掉、不存在的忽略, 天然幂等。
        recordMapper.delete(new LambdaQueryWrapper<ContainerTrailerRecord>()
                .in(ContainerTrailerRecord::getId, distinctIds));
    }

    private ContainerTrailerRecord getExisting(Long id) {
        ContainerTrailerRecord e = recordMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("提空箱记录不存在");
        }
        return e;
    }

    /**
     * 箱号必须存在且**未删除**。
     * 已删除的集装箱还在库里(为了保留历史记录), 但不能再拿来提空箱。
     */
    private void validateContainerUsable(String containerNo) {
        Container container = containerMapper.selectById(containerNo);
        if (container == null) {
            throw new BizException("集装箱不存在", "containerNo=" + containerNo);
        }
        if (ContainerStatusConstants.isDeleted(container.getStatusId())) {
            throw new BizException("集装箱已删除, 不能登记提空箱: " + containerNo);
        }
    }

    private void validateTrailerExists(String trackNo) {
        if (trailerMapper.selectById(trackNo) == null) {
            throw new BizException("拖车不存在", "trackNo=" + trackNo);
        }
    }

    /** 当前页里出现过的拖车号(去重) */
    private List<String> collectTrackNos(List<ContainerTrailerRecord> records) {
        return records.stream()
                .map(ContainerTrailerRecord::getTrackNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /** 一次查出这批拖车的号 -> 司机姓名。空集合直接返回空表, 不发查询 */
    private Map<String, String> findDriverNames(List<String> trackNos) {
        if (trackNos.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<Trailer> w = new LambdaQueryWrapper<>();
        w.select(Trailer::getNo, Trailer::getName);
        w.in(Trailer::getNo, trackNos);
        // 用 HashMap 而不是 Collectors.toMap: toMap 不允许 value 为 null
        Map<String, String> names = new HashMap<>();
        for (Trailer t : trailerMapper.selectList(w)) {
            names.put(t.getNo(), t.getName());
        }
        return names;
    }
}
