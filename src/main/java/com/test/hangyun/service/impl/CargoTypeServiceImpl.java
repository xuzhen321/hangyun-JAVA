package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CargoTypeQueryReq;
import com.test.hangyun.dto.CargoTypeReq;
import com.test.hangyun.dto.vo.CargoTypeOptionVO;
import com.test.hangyun.dto.vo.CargoTypeVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.CargoTypeMapper;
import com.test.hangyun.pojo.entity.CargoType;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.CargoTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 货物种类管理。
 * <p>
 * 两张表的关系: cargo_type 是字典, cargo.cargo_type_id 逻辑引用它。
 * 库里没有物理外键, 所以删除前必须自己挡住被引用的记录。
 * insert_time / update_time 交给触发器, 应用层不赋值。
 */
@Service
@RequiredArgsConstructor
public class CargoTypeServiceImpl implements CargoTypeService {

    private final CargoTypeMapper cargoTypeMapper;

    @Override
    public PageResult<CargoTypeVO> page(CargoTypeQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<CargoType> w = new LambdaQueryWrapper<>();
        // 前缀匹配, 用 likeRight 而不是 like: 只有"从头匹配"(name LIKE 'x%')才可能走 name 上的
        // varchar_pattern_ops 索引, 两边都带 % 只能全表扫描。
        if (StringUtils.hasText(req.getName())) {
            w.likeRight(CargoType::getName, req.getName().trim());
        }
        w.orderByAsc(CargoType::getId);

        Page<CargoType> p = cargoTypeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, CargoTypeVO::from);
    }

    @Override
    public CargoTypeVO getById(Long id) {
        return CargoTypeVO.from(getExisting(id));
    }

    @Override
    public List<CargoTypeOptionVO> options(String name) {
        // 和 /customers/options、/ports/options 不同, 这里**不设条数上限**:
        // 货物种类是字典表, 条数少, 下拉框是一次性加载让人挑的, 截断到 20 条反而会把
        // 后面的种类藏起来、搜都搜不到。将来种类多到影响响应了再改成远程搜索。
        LambdaQueryWrapper<CargoType> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            w.likeRight(CargoType::getName, name.trim());
        }
        w.orderByAsc(CargoType::getId);
        return cargoTypeMapper.selectList(w).stream()
                .map(CargoTypeOptionVO::from)
                .toList();
    }

    @OpLog(module = "货物种类", table = "cargo_type", type = OpType.INSERT, desc = "新增货物种类")
    @Override
    @Transactional
    public void create(CargoTypeReq req) {
        CargoType e = new CargoType();
        e.setName(req.getName().trim());
        e.setDescription(req.getDescription());
        e.setWeightKg(req.getWeightKg());
        e.setVolumeM3(req.getVolumeM3());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        cargoTypeMapper.insert(e);
    }

    @OpLog(module = "货物种类", table = "cargo_type", type = OpType.UPDATE, desc = "修改货物种类")
    @Override
    @Transactional
    public void update(Long id, CargoTypeReq req) {
        getExisting(id);

        // 用 UpdateWrapper 显式列出要改的列:
        //  1. 后端传的 null 能真正写进去(把字段清空), 不会被 "非空才更新" 策略跳过
        //  2. 结构上保证不会碰到 insert_time / update_time
        LambdaUpdateWrapper<CargoType> u = new LambdaUpdateWrapper<>();
        u.eq(CargoType::getId, id)
                .set(CargoType::getName, req.getName().trim())
                .set(CargoType::getDescription, req.getDescription())
                .set(CargoType::getWeightKg, req.getWeightKg())
                .set(CargoType::getVolumeM3, req.getVolumeM3());
        cargoTypeMapper.update(null, u);
    }

    @OpLog(module = "货物种类", table = "cargo_type", type = OpType.DELETE, desc = "删除货物种类")
    @Override
    @Transactional
    public void delete(Long id) {
        CargoType existing = getExisting(id);
        // 这里是物理删除, 必须挡住仍被货物引用的记录, 否则 cargo 会留下悬空的 cargo_type_id
        if (!findReferencedNames(List.of(id)).isEmpty()) {
            throw BizException.conflict("该货物种类下还有货物, 无法删除: " + existing.getName());
        }
        cargoTypeMapper.deleteById(id);
    }

    @OpLog(module = "货物种类", table = "cargo_type", type = OpType.DELETE, desc = "批量删除货物种类")
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 去重: 前端多选时可能传进重复的 id
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }

        // 严格语义: 只要有一个被引用, 整批拒绝。
        // 不像客户/订单那样"宽松跳过" —— 那两个是逻辑删除, 跳过不影响什么;
        // 这里是物理删除, 悄悄留下几条没删掉的话, 用户会以为全删成功了。
        List<String> blocked = findReferencedNames(distinctIds);
        if (!blocked.isEmpty()) {
            // 提示里用名称: 名称是用户看得懂的业务数据, 不把主键写进返回给前端的文案
            throw BizException.conflict("以下货物种类下还有货物, 无法删除: " + String.join("、", blocked));
        }

        cargoTypeMapper.delete(new LambdaQueryWrapper<CargoType>().in(CargoType::getId, distinctIds));
    }

    private CargoType getExisting(Long id) {
        CargoType e = cargoTypeMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("货物种类不存在");
        }
        return e;
    }

    /**
     * 从这批 id 里挑出「已被货物引用」的那些, 返回它们的名称。
     * <p>
     * 一次查询搞定: {@code id IN (给定列表) AND id IN (SELECT cargo_type_id FROM cargo)}。
     * 前半段把范围限在"这次要删的几个"里, 后半段只留下"确实被引用"的,
     * 二者的交集就是拦截名单。返回空表示这批都可以删。
     */
    private List<String> findReferencedNames(List<Long> ids) {
        LambdaQueryWrapper<CargoType> w = new LambdaQueryWrapper<>();
        w.select(CargoType::getName);
        w.in(CargoType::getId, ids);
        // 子查询是写死的常量, 没有拼接用户输入
        w.inSql(CargoType::getId, "select cargo_type_id from cargo");
        return cargoTypeMapper.selectList(w).stream()
                .map(CargoType::getName)
                .toList();
    }
}
