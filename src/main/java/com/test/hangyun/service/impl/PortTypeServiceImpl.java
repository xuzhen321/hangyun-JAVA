package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.PortTypeQueryReq;
import com.test.hangyun.dto.PortTypeReq;
import com.test.hangyun.dto.vo.PortTypeOptionVO;
import com.test.hangyun.dto.vo.PortTypeVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.PortTypeMapper;
import com.test.hangyun.pojo.entity.PortType;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.PortTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 港口类型字典。字段没有唯一约束, 不查重; 删除前查引用(港口)。 */
@Service
@RequiredArgsConstructor
public class PortTypeServiceImpl implements PortTypeService {

    private final PortTypeMapper portTypeMapper;

    @Override
    public PageResult<PortTypeVO> page(PortTypeQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<PortType> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            w.likeRight(PortType::getType, req.getKeyword().trim());
        }
        w.orderByAsc(PortType::getId);

        Page<PortType> p = portTypeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, PortTypeVO::from);
    }

    @Override
    public PortTypeVO getById(Long id) {
        return PortTypeVO.from(getExisting(id));
    }

    @Override
    public List<PortTypeOptionVO> options() {
        return portTypeMapper.selectList(
                        new LambdaQueryWrapper<PortType>().orderByAsc(PortType::getId))
                .stream().map(PortTypeOptionVO::from).toList();
    }

    @OpLog(module = "港口类型", table = "port_type", type = OpType.INSERT, desc = "新增港口类型")
    @Override
    @Transactional
    public void create(PortTypeReq req) {
        PortType e = new PortType();
        e.setType(req.getType().trim());
        portTypeMapper.insert(e);
    }

    @Override
    @Transactional
    public void update(Long id, PortTypeReq req) {
        getExisting(id);

        LambdaUpdateWrapper<PortType> u = new LambdaUpdateWrapper<>();
        u.eq(PortType::getId, id).set(PortType::getType, req.getType().trim());
        portTypeMapper.update(null, u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);
        long ports = portTypeMapper.countPortsByPortTypeId(id);
        if (ports > 0) {
            throw BizException.conflict("该类型下有 " + ports + " 个港口, 无法删除");
        }
        portTypeMapper.deleteById(id);
    }

    private PortType getExisting(Long id) {
        PortType e = portTypeMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("港口类型不存在");
        }
        return e;
    }
}
