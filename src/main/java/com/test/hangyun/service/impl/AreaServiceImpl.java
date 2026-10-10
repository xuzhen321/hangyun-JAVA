package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.AreaQueryReq;
import com.test.hangyun.dto.AreaReq;
import com.test.hangyun.dto.vo.AreaOptionVO;
import com.test.hangyun.dto.vo.AreaVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.AreaMapper;
import com.test.hangyun.pojo.entity.Area;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 区域字典。area_name 唯一, 删除前查引用(港口)。 */
@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaMapper areaMapper;

    @Override
    public PageResult<AreaVO> page(AreaQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Area> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            w.likeRight(Area::getAreaName, req.getKeyword().trim());
        }
        w.orderByAsc(Area::getId);

        Page<Area> p = areaMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, AreaVO::from);
    }

    @Override
    public AreaVO getById(Long id) {
        return AreaVO.from(getExisting(id));
    }

    @Override
    public List<AreaOptionVO> options() {
        return areaMapper.selectList(new LambdaQueryWrapper<Area>().orderByAsc(Area::getId))
                .stream().map(AreaOptionVO::from).toList();
    }

    @OpLog(module = "区域字典", table = "area", type = OpType.INSERT, desc = "新增区域")
    @Override
    @Transactional
    public void create(AreaReq req) {
        String name = req.getAreaName().trim();
        ensureNameUnique(name, null);

        Area e = new Area();
        e.setAreaName(name);
        areaMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, AreaReq req) {
        getExisting(id);
        String name = req.getAreaName().trim();
        ensureNameUnique(name, id);

        LambdaUpdateWrapper<Area> u = new LambdaUpdateWrapper<>();
        u.eq(Area::getId, id).set(Area::getAreaName, name);
        areaMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        long ports = areaMapper.countPortsByAreaId(id);
        if (ports > 0) {
            throw BizException.conflict("该区域下有 " + ports + " 个港口, 无法删除");
        }
        areaMapper.deleteById(id);
    }

    private Area getExisting(Long id) {
        Area e = areaMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("区域不存在");
        }
        return e;
    }

    /** area_name 有唯一约束, 提前查重以便返回 409 而不是撞唯一键报 500 */
    private void ensureNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Area> w = new LambdaQueryWrapper<>();
        w.eq(Area::getAreaName, name);
        w.ne(excludeId != null, Area::getId, excludeId);
        if (areaMapper.selectCount(w) > 0) {
            throw BizException.conflict("区域名称已存在: " + name);
        }
    }
}
