package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ContainerStatusConstants;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CargoContainerResultQueryReq;
import com.test.hangyun.dto.CargoContainerResultReq;
import com.test.hangyun.dto.vo.CargoContainerResultVO;
import com.test.hangyun.mapper.CargoContainerResultMapper;
import com.test.hangyun.mapper.CargoMapper;
import com.test.hangyun.mapper.CargoTypeMapper;
import com.test.hangyun.mapper.ContainerMapper;
import com.test.hangyun.pojo.entity.Cargo;
import com.test.hangyun.pojo.entity.CargoContainerResult;
import com.test.hangyun.pojo.entity.CargoType;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.Container;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.CargoContainerResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 货物装箱结果管理。
 * <p>
 * 库里**没有**这个表的视图, 所以直接读基础表。麻烦在于出参里的"货物名称"和"订单号"
 * 都不在本表上, 链路是:
 * <pre>
 *   cargo_container_result --cargo_id--> cargo --cargo_type_id--> cargo_type.name
 *                                            \--order_id--> orders.id
 * </pre>
 * 没有视图就没法一次联表查完, 只能由 Service 分步:
 * <ol>
 *   <li><b>筛选时</b>: 按货物名称前缀/订单号先查出符合条件的 {@code cargo.id}, 用它去筛装箱结果</li>
 *   <li><b>出参时</b>: 拿到当前页后, 收集 {@code cargo_id} 一次查出 cargo(顺带拿到订单号),
 *       再把用到的种类 id 一次查出名称, 回填到 VO</li>
 * </ol>
 * 库里没有物理外键, 所以货物的存在性由应用层校验。
 */
@Service
@RequiredArgsConstructor
public class CargoContainerResultServiceImpl implements CargoContainerResultService {

    private final CargoContainerResultMapper cargoContainerResultMapper;
    private final CargoMapper cargoMapper;
    private final CargoTypeMapper cargoTypeMapper;
    private final ContainerMapper containerMapper;

    @Override
    public PageResult<CargoContainerResultVO> page(CargoContainerResultQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<CargoContainerResult> w = buildWrapper(req);
        w.orderByAsc(CargoContainerResult::getId);

        Page<CargoContainerResult> p =
                cargoContainerResultMapper.selectPage(new Page<>(pageNo, pageSize), w);

        // 第二步: 回填货物名称和订单号
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(), assemble(p.getRecords()));
    }

    @Override
    @OpLog(module = "货物装箱结果", table = "cargo_container_result", type = OpType.EXPORT, desc = "导出 Excel")
    public List<CargoContainerResultVO> listForExport(CargoContainerResultQueryReq req) {
        LambdaQueryWrapper<CargoContainerResult> w = buildWrapper(req);
        w.orderByAsc(CargoContainerResult::getId);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<CargoContainerResult> rows = cargoContainerResultMapper.selectList(
                w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return assemble(rows);
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<CargoContainerResult> buildWrapper(CargoContainerResultQueryReq req) {
        LambdaQueryWrapper<CargoContainerResult> w = new LambdaQueryWrapper<>();

        // 第一步: "货物名称"和"订单号"都不在本表上, 先拿它们筛出符合条件的 cargo.id。
        // 两个条件是 AND 的关系, 所以放在同一次 cargo 查询里, 不用查两遍。
        String namePrefix = StringUtils.hasText(req.getCargoTypeName())
                ? req.getCargoTypeName().trim() : null;
        String orderId = StringUtils.hasText(req.getOrderId())
                ? req.getOrderId().trim() : null;
        if (namePrefix != null || orderId != null) {
            List<Long> cargoIds = findCargoIds(namePrefix, orderId);
            if (cargoIds.isEmpty()) {
                // 没有货物符合条件, 结果必然为空。用恒假条件表达"空结果", 让列表和导出共用同一个
                // wrapper —— 空集合传给 MP 的 in() 会拼出 "IN ()", 是非法 SQL。
                w.apply("1 = 0");
                return w;
            }
            w.in(CargoContainerResult::getCargoId, cargoIds);
        }

        // 集装箱号是本表的列, 直接精确匹配
        w.eq(StringUtils.hasText(req.getContainerNo()),
                CargoContainerResult::getContainerNo, req.getContainerNo());
        return w;
    }

    /** 列表和导出共用的组装: 批量查货物后回填货物名称和订单号 */
    private List<CargoContainerResultVO> assemble(List<CargoContainerResult> records) {
        Map<Long, Cargo> cargoById = findCargos(collectCargoIds(records));
        Map<Long, String> typeNames = findTypeNames(cargoById.values());
        return records.stream().map(r -> {
            Cargo cargo = cargoById.get(r.getCargoId());
            // 数据正常时 cargo 一定查得到(cargo_id 是 not null, 且删货物会被引用检查挡住)。
            // 万一有人直接改库把 cargo 删了, 这里降级成 null 而不是抛异常, 免得整个列表打不开。
            if (cargo == null) {
                return CargoContainerResultVO.from(r, null, null);
            }
            return CargoContainerResultVO.from(r,
                    typeNames.get(cargo.getCargoTypeId()), cargo.getOrderId());
        }).toList();
    }

    @Override
    public CargoContainerResultVO getById(Long id) {
        CargoContainerResult e = getExisting(id);
        Map<Long, Cargo> cargoById = findCargos(List.of(e.getCargoId()));
        Cargo cargo = cargoById.get(e.getCargoId());
        if (cargo == null) {
            return CargoContainerResultVO.from(e, null, null);
        }
        return CargoContainerResultVO.from(e,
                findTypeNames(cargoById.values()).get(cargo.getCargoTypeId()),
                cargo.getOrderId());
    }

    @Override
    @OpLog(module = "货物装箱结果", table = "cargo_container_result", type = OpType.INSERT, desc = "新增装箱结果")
    @Transactional
    public void create(CargoContainerResultReq req) {
        String containerNo = req.getContainerNo().trim();
        validateCargoExists(req.getCargoId());
        validateContainerExists(containerNo);
        // 新增: 没有"自己"要排除
        validateQuantityWithinLimit(req.getCargoId(), req.getQuantity(), null);

        CargoContainerResult e = new CargoContainerResult();
        e.setCargoId(req.getCargoId());
        e.setContainerNo(containerNo);
        e.setQuantity(req.getQuantity());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        cargoContainerResultMapper.insert(e);
    }

    @Override
    @OpLog(module = "货物装箱结果", table = "cargo_container_result", type = OpType.UPDATE, desc = "修改装箱结果")
    @Transactional
    public void update(Long id, CargoContainerResultReq req) {
        getExisting(id);
        String containerNo = req.getContainerNo().trim();
        validateCargoExists(req.getCargoId());
        validateContainerExists(containerNo);
        // 修改: 要把**自己原来占的那部分**排除掉, 否则"数量不变只换个箱子"也会被误判超量
        validateQuantityWithinLimit(req.getCargoId(), req.getQuantity(), id);

        // 整体覆盖: 三个字段都必填(库里都是 not null), 所以 UPDATE 里全都显式 set,
        // 结构上也保证碰不到 insert_time / update_time。
        LambdaUpdateWrapper<CargoContainerResult> u = new LambdaUpdateWrapper<>();
        u.eq(CargoContainerResult::getId, id)
                .set(CargoContainerResult::getCargoId, req.getCargoId())
                .set(CargoContainerResult::getContainerNo, containerNo)
                .set(CargoContainerResult::getQuantity, req.getQuantity());
        cargoContainerResultMapper.update(null, u);
    }

    @Override
    @OpLog(module = "货物装箱结果", table = "cargo_container_result", type = OpType.DELETE, desc = "删除装箱结果")
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        // 物理删除。这张表是"货物装进集装箱"的结果, 没有被别的表引用, 所以不需要引用检查。
        cargoContainerResultMapper.deleteById(id);
    }

    @Override
    @OpLog(module = "货物装箱结果", table = "cargo_container_result", type = OpType.DELETE, desc = "批量删除装箱结果")
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 去重: 前端多选时可能传进重复的 id
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        // 没有引用保护, 所以批量这边不需要"严格/宽松"的取舍: 存在的删掉, 不存在的忽略, 天然幂等。
        cargoContainerResultMapper.delete(
                new LambdaQueryWrapper<CargoContainerResult>()
                        .in(CargoContainerResult::getId, distinctIds));
    }

    private CargoContainerResult getExisting(Long id) {
        CargoContainerResult e = cargoContainerResultMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("装箱记录不存在");
        }
        return e;
    }

    private void validateCargoExists(Long cargoId) {
        if (cargoMapper.selectById(cargoId) == null) {
            // 文案里不带 id; 具体值走 detail, 只进服务端日志
            throw new BizException("货物不存在", "cargoId=" + cargoId);
        }
    }

    /**
     * 箱号必须在 container 表里真实存在, 否则会登记出一条挂在"不存在"的箱子上的记录。
     * <p>
     * 另外**已删除**的集装箱也不能再装货: 它虽然还在库里(为了保留历史记录), 但已经不可用了。
     */
    private void validateContainerExists(String containerNo) {
        Container container = containerMapper.selectById(containerNo);
        if (container == null) {
            throw new BizException("集装箱不存在", "containerNo=" + containerNo);
        }
        if (ContainerStatusConstants.isDeleted(container.getStatusId())) {
            throw new BizException("集装箱已删除, 不能装箱: " + containerNo);
        }
    }

    /**
     * 校验「同一批货物的装箱总量 ≤ 该货物的数量」。
     * <p>
     * 货有多少件是 cargo.quantity 定的, 装箱是把这堆货分装到各个箱子里,
     * 所以所有装箱记录的 quantity 加起来不能超过它 —— 否则就是"装出"了不存在的货。
     * <p>
     * 修改时要传 {@code excludeRecordId}: 这条记录自己原来占的量已经在"已装总量"里了,
     * 不排除掉的话, "数量不变、只换个箱子"也会被算成超量。
     * <p>
     * 报错里给出**最多还能装多少**, 前端可以直接展示给用户。
     */
    private void validateQuantityWithinLimit(Long cargoId, Integer newQuantity, Long excludeRecordId) {
        Cargo cargo = cargoMapper.selectById(cargoId);
        int total = cargo.getQuantity();

        long loaded = cargoContainerResultMapper.sumQuantityByCargoId(cargoId);
        // 这次要改的那条记录自己占了多少(新增时是 0)。
        // 只有它当前就挂在这批货上, 才需要从"已装"里减掉。
        long selfOccupied = 0;
        if (excludeRecordId != null) {
            CargoContainerResult old = cargoContainerResultMapper.selectById(excludeRecordId);
            if (old != null && cargoId.equals(old.getCargoId())) {
                selfOccupied = old.getQuantity();
            }
        }
        long alreadyOther = loaded - selfOccupied;
        long remaining = total - alreadyOther;

        if (newQuantity > remaining) {
            // 只说"共多少件、最多还能装多少"。不报"已装多少"是因为修改时那个数
            // 排除了本条记录自己, 前端看到 2 条记录合计 80 而提示说"已装 40"会更困惑。
            throw BizException.conflict("装入数量超出该货物的可装总量: 这批货物共 " + total
                    + " 件, 最多还能装 " + remaining + " 件");
        }
    }

    /**
     * 按货物名称前缀 / 订单号筛出 cargo.id。
     * <p>
     * 名称要先经 cargo_type 换成种类 id 才能筛 cargo, 所以最多两跳;
     * 订单号是 cargo 自己的列, 和名称一起放在同一次查询里(AND)。
     */
    private List<Long> findCargoIds(String namePrefix, String orderId) {
        LambdaQueryWrapper<Cargo> w = new LambdaQueryWrapper<>();
        w.select(Cargo::getId);

        if (namePrefix != null) {
            LambdaQueryWrapper<CargoType> tw = new LambdaQueryWrapper<>();
            tw.select(CargoType::getId);
            // 前缀匹配, 走的是 cargo_type.name 上的 varchar_pattern_ops 索引
            tw.likeRight(CargoType::getName, namePrefix);
            List<Long> typeIds = cargoTypeMapper.selectList(tw).stream()
                    .map(CargoType::getId).toList();
            if (typeIds.isEmpty()) {
                return List.of();
            }
            w.in(Cargo::getCargoTypeId, typeIds);
        }

        w.eq(orderId != null, Cargo::getOrderId, orderId);

        return cargoMapper.selectList(w).stream().map(Cargo::getId).toList();
    }

    /** 当前页里出现过的 cargo_id(去重) */
    private List<Long> collectCargoIds(List<CargoContainerResult> records) {
        return records.stream()
                .map(CargoContainerResult::getCargoId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /** 一次查出这批货物(顺带拿到 cargo_type_id 和 order_id) */
    private Map<Long, Cargo> findCargos(List<Long> cargoIds) {
        if (cargoIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<Cargo> w = new LambdaQueryWrapper<>();
        w.select(Cargo::getId, Cargo::getCargoTypeId, Cargo::getOrderId);
        w.in(Cargo::getId, cargoIds);
        return cargoMapper.selectList(w).stream()
                .collect(Collectors.toMap(Cargo::getId, Function.identity()));
    }

    /** 一次查出这批货物用到的种类名称(id -> name) */
    private Map<Long, String> findTypeNames(Collection<Cargo> cargos) {
        List<Long> typeIds = cargos.stream()
                .map(Cargo::getCargoTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (typeIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<CargoType> w = new LambdaQueryWrapper<>();
        w.select(CargoType::getId, CargoType::getName);
        w.in(CargoType::getId, typeIds);
        // 用 HashMap 而不是 Collectors.toMap: toMap 不允许 value 为 null, 而名称理论上可能查不到
        Map<Long, String> names = new HashMap<>();
        for (CargoType t : cargoTypeMapper.selectList(w)) {
            names.put(t.getId(), t.getName());
        }
        return names;
    }
}
