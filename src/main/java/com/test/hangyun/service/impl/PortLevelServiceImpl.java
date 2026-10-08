package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.PortLevelQueryReq;
import com.test.hangyun.dto.PortLevelReq;
import com.test.hangyun.dto.vo.PortLevelOptionVO;
import com.test.hangyun.dto.vo.PortLevelVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.PortLevelMapper;
import com.test.hangyun.pojo.entity.PortLevel;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.PortLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 港口级别字典。字段没有唯一约束, 不查重; 删除前查引用(港口)。 */
@Service
@RequiredArgsConstructor
public class PortLevelServiceImpl implements PortLevelService {

    private final PortLevelMapper portLevelMapper;

    @Override
    public PageResult<PortLevelVO> page(PortLevelQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<PortLevel> w = new LambdaQueryWrapper<>();
        w.orderByAsc(PortLevel::getId);

        Page<PortLevel> p = portLevelMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, PortLevelVO::from);
    }

    @Override
    public PortLevelVO getById(Long id) {
        return PortLevelVO.from(getExisting(id));
    }

    @Override
    public List<PortLevelOptionVO> options() {
        return portLevelMapper.selectList(
                        new LambdaQueryWrapper<PortLevel>().orderByAsc(PortLevel::getId))
                .stream().map(PortLevelOptionVO::from).toList();
    }

    @OpLog(module = "港口级别", table = "port_level", type = OpType.INSERT, desc = "新增港口级别")
    @Override
    @Transactional
    public void create(PortLevelReq req) {
        PortLevel e = new PortLevel();
        e.setLevel(req.getLevel());
        portLevelMapper.insert(e);
    }

    @OpLog(module = "港口级别", table = "port_level", type = OpType.UPDATE, desc = "修改港口级别")
    @Override
    @Transactional
    public void update(Long id, PortLevelReq req) {
        getExisting(id);

        LambdaUpdateWrapper<PortLevel> u = new LambdaUpdateWrapper<>();
        u.eq(PortLevel::getId, id).set(PortLevel::getLevel, req.getLevel());
        portLevelMapper.update(null, u);
    }

    @OpLog(module = "港口级别", table = "port_level", type = OpType.DELETE, desc = "删除港口级别")
    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        long ports = portLevelMapper.countPortsByLevelId(id);
        if (ports > 0) {
            throw BizException.conflict("该级别下有 " + ports + " 个港口, 无法删除");
        }
        portLevelMapper.deleteById(id);
    }

    private PortLevel getExisting(Long id) {
        PortLevel e = portLevelMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("港口级别不存在");
        }
        return e;
    }
}
