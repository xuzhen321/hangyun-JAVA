package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CargoCreateReq;
import com.test.hangyun.dto.CargoQueryReq;
import com.test.hangyun.dto.CargoUpdateReq;
import com.test.hangyun.dto.vo.CargoOptionVO;
import com.test.hangyun.dto.vo.CargoVO;
import com.test.hangyun.mapper.CargoMapper;
import com.test.hangyun.mapper.CargoTypeMapper;
import com.test.hangyun.mapper.OrderMapper;
import com.test.hangyun.pojo.entity.Cargo;
import com.test.hangyun.pojo.entity.CargoType;
import com.test.hangyun.service.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单货物。
 * <p>
 * 库里**没有** v_cargo 视图, 所以直接读基础表 cargo。
 * 麻烦之处在于"按货物名称筛选"和"显示货物名称"这两个需求落在 cargo_type 表上,
 * 而 cargo 里只有 cargo_type_id —— 没有视图就没法一次联表查完, 只能在 Service 层分两步:
 * <ol>
 *   <li>按名称前缀查出匹配的 cargo_type.id, 用它去筛 cargo</li>
 *   <li>拿到当前页的 cargo 后, 把用到的 cargo_type.id 收集起来一次查出名称, 回填到 VO</li>
 * </ol>
 * 库里没有物理外键, 所以订单、货物种类的存在性都由应用层校验。
 */
@Service
@RequiredArgsConstructor
public class CargoServiceImpl implements CargoService {

    private final CargoMapper cargoMapper;
    private final CargoTypeMapper cargoTypeMapper;
    private final OrderMapper orderMapper;

    @Override
    public PageResult<CargoVO> page(CargoQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Cargo> w = new LambdaQueryWrapper<>();

        // 第一步: 货物名称在 cargo_type 上, 先按前缀把种类 id 查出来
        if (StringUtils.hasText(req.getCargoTypeName())) {
            List<Long> typeIds = findCargoTypeIdsByNamePrefix(req.getCargoTypeName().trim());
            if (typeIds.isEmpty()) {
                // 没有任何种类匹配这个前缀, 结果必然为空, 直接短路。
                // 这同时也避开了一个坑: 空集合传给 MP 的 in() 会拼出 "IN ()", 是非法 SQL。
                return new PageResult<>(0, pageNo, pageSize, List.of());
            }
            w.in(Cargo::getCargoTypeId, typeIds);
        }

        // 订单号是标识符, 用精确匹配
        w.eq(StringUtils.hasText(req.getOrderId()), Cargo::getOrderId, req.getOrderId());

        // 按 id 升序: 这是订单下的明细行, 按录入顺序展示更自然也稳定
        // (客户、订单那种顶层业务列表才用"最新在前")
        w.orderByAsc(Cargo::getId);

        Page<Cargo> p = cargoMapper.selectPage(new Page<>(pageNo, pageSize), w);

        // 第二步: 把当前页用到的货物种类名称一次查出来, 回填到 VO
        Map<Long, String> typeNames = findTypeNames(collectTypeIds(p.getRecords()));
        return PageResult.of(p, (Function<Cargo, CargoVO>)
                c -> CargoVO.from(c, typeNames.get(c.getCargoTypeId())));
    }

    @Override
    public List<CargoOptionVO> options(String cargoTypeName, String orderId) {
        LambdaQueryWrapper<Cargo> w = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(cargoTypeName)) {
            List<Long> typeIds = findCargoTypeIdsByNamePrefix(cargoTypeName.trim());
            if (typeIds.isEmpty()) {
                // 没有种类匹配这个前缀; 同时避开空集合传给 in() 会拼出 "IN ()" 的坑
                return List.of();
            }
            w.in(Cargo::getCargoTypeId, typeIds);
        }
        w.eq(StringUtils.hasText(orderId), Cargo::getOrderId, orderId);
        w.orderByAsc(Cargo::getId);

        // searchCount=false: 下拉框不需要 total, 省掉那条 COUNT
        List<Cargo> records = cargoMapper.selectPage(
                new Page<>(1, OptionConstants.OPTION_LIMIT, false), w).getRecords();

        Map<Long, String> typeNames = findTypeNames(collectTypeIds(records));
        return records.stream()
                .map(c -> CargoOptionVO.of(c, typeNames.get(c.getCargoTypeId())))
                .toList();
    }

    @Override
    @Transactional
    public void create(CargoCreateReq req) {
        // 先 trim 再校验: 订单号常从页面上复制粘贴, 首尾可能带空格。
        // 如果用没 trim 的值去校验、用 trim 的值去入库(或反过来), 会出现"校验通过但存了个查不到的订单号"。
        String orderId = req.getOrderId().trim();
        validateOrderExists(orderId);
        validateCargoTypeExists(req.getCargoTypeId());

        Cargo e = new Cargo();
        e.setOrderId(orderId);
        e.setCargoTypeId(req.getCargoTypeId());
        e.setQuantity(req.getQuantity());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        cargoMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, CargoUpdateReq req) {
        getExisting(id);
        validateCargoTypeExists(req.getCargoTypeId());

        // 显式列出要改的列 —— 结构上碰不到 order_id, 所以"所属订单不可改"是代码保证的,
        // 不只是"请求体里没有这个字段"。要换订单: 删掉这条, 到目标订单下重新加。
        LambdaUpdateWrapper<Cargo> u = new LambdaUpdateWrapper<>();
        u.eq(Cargo::getId, id)
                .set(Cargo::getCargoTypeId, req.getCargoTypeId())
                .set(Cargo::getQuantity, req.getQuantity());
        cargoMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        // 物理删除, 必须挡住仍被装箱结果引用的记录, 否则 cargo_container_result 会留下悬空的 cargo_id
        if (cargoMapper.countReferencedByIds(List.of(id)) > 0) {
            throw BizException.conflict("该货物已被装箱记录引用, 无法删除");
        }
        cargoMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 去重: 前端多选时可能传进重复的 id
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }

        // 严格语义: 只要有一条被引用, 整批拒绝, 不做部分删除。
        // 货物是物理删除, 悄悄留下几条没删掉的话, 用户会以为全删成功了。
        long referenced = cargoMapper.countReferencedByIds(distinctIds);
        if (referenced > 0) {
            throw BizException.conflict("选中的货物中有 " + referenced + " 条已被装箱记录引用, 无法删除");
        }

        cargoMapper.delete(new LambdaQueryWrapper<Cargo>().in(Cargo::getId, distinctIds));
    }

    private Cargo getExisting(Long id) {
        Cargo e = cargoMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("货物不存在");
        }
        return e;
    }

    private void validateOrderExists(String orderId) {
        if (orderMapper.selectById(orderId) == null) {
            // 文案里不带订单号; 具体值走 detail, 只进服务端日志
            throw new BizException("订单不存在", "orderId=" + orderId);
        }
    }

    /** cargo_type_id 在库里是 not null、DTO 上也是 @NotNull, 所以这里不用再判空 */
    private void validateCargoTypeExists(Long cargoTypeId) {
        if (cargoTypeMapper.selectById(cargoTypeId) == null) {
            throw new BizException("货物种类不存在", "cargoTypeId=" + cargoTypeId);
        }
    }

    /** 按名称前缀查货物种类 id。走的是 cargo_type.name 上的 varchar_pattern_ops 索引 */
    private List<Long> findCargoTypeIdsByNamePrefix(String namePrefix) {
        LambdaQueryWrapper<CargoType> w = new LambdaQueryWrapper<>();
        w.select(CargoType::getId);
        w.likeRight(CargoType::getName, namePrefix);
        return cargoTypeMapper.selectList(w).stream().map(CargoType::getId).toList();
    }

    /** 当前页里出现过的 cargo_type_id(去重, 去掉 null —— 货物可以不挂种类) */
    private List<Long> collectTypeIds(List<Cargo> records) {
        return records.stream()
                .map(Cargo::getCargoTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /** 一次查出这批种类的 id -> 名称。空集合直接返回空表, 不发查询 */
    private Map<Long, String> findTypeNames(List<Long> typeIds) {
        if (typeIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<CargoType> w = new LambdaQueryWrapper<>();
        w.select(CargoType::getId, CargoType::getName);
        w.in(CargoType::getId, typeIds);
        return cargoTypeMapper.selectList(w).stream()
                .collect(Collectors.toMap(CargoType::getId, CargoType::getName));
    }
}
