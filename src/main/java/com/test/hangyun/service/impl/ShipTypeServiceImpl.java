package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.ShipTypeQueryReq;
import com.test.hangyun.dto.ShipTypeReq;
import com.test.hangyun.dto.vo.ShipTypeOptionVO;
import com.test.hangyun.dto.vo.ShipTypeVO;
import com.test.hangyun.mapper.ShipTypeMapper;
import com.test.hangyun.pojo.entity.ShipType;
import com.test.hangyun.service.ShipTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 船舶类型字典维护。
 * <p>
 * 字段都没有唯一约束(允许重复的船型), 所以不用查重; 但 vessel.vessel_type_id 引用它,
 * 删除前必须查引用。insert_time / update_time 交给触发器。
 */
@Service
@RequiredArgsConstructor
public class ShipTypeServiceImpl implements ShipTypeService {

    private final ShipTypeMapper shipTypeMapper;

    @Override
    public PageResult<ShipTypeVO> page(ShipTypeQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<ShipType> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            w.likeRight(ShipType::getType, req.getKeyword().trim());
        }
        w.orderByAsc(ShipType::getId);

        Page<ShipType> p = shipTypeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, ShipTypeVO::from);
    }

    @Override
    public ShipTypeVO getById(Long id) {
        return ShipTypeVO.from(getExisting(id));
    }

    @Override
    public List<ShipTypeOptionVO> options() {
        // 字典表条数少, 一次性返回全部, 不设上限
        return shipTypeMapper.selectList(
                        new LambdaQueryWrapper<ShipType>().orderByAsc(ShipType::getId))
                .stream().map(ShipTypeOptionVO::from).toList();
    }

    @Override
    @Transactional
    public void create(ShipTypeReq req) {
        ShipType e = new ShipType();
        e.setType(req.getType().trim());
        e.setGt(req.getGt());
        e.setLength(req.getLength());
        e.setWidth(req.getWidth());
        e.setNt(req.getNt());
        e.setDwt(req.getDwt());
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        shipTypeMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, ShipTypeReq req) {
        getExisting(id);

        // 整体覆盖: 每个字段都显式 set(null 也能真正写进去)
        LambdaUpdateWrapper<ShipType> u = new LambdaUpdateWrapper<>();
        u.eq(ShipType::getId, id)
                .set(ShipType::getType, req.getType().trim())
                .set(ShipType::getGt, req.getGt())
                .set(ShipType::getLength, req.getLength())
                .set(ShipType::getWidth, req.getWidth())
                .set(ShipType::getNt, req.getNt())
                .set(ShipType::getDwt, req.getDwt());
        shipTypeMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被船舶引用的船型
        long vessels = shipTypeMapper.countVesselsByTypeId(id);
        if (vessels > 0) {
            throw BizException.conflict("该船型下存在 " + vessels + " 艘船, 无法删除");
        }
        shipTypeMapper.deleteById(id);
    }

    private ShipType getExisting(Long id) {
        ShipType e = shipTypeMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("船舶类型不存在");
        }
        return e;
    }
}
