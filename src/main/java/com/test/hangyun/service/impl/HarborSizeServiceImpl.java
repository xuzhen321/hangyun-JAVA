package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.HarborSizeQueryReq;
import com.test.hangyun.dto.HarborSizeReq;
import com.test.hangyun.dto.vo.HarborSizeOptionVO;
import com.test.hangyun.dto.vo.HarborSizeVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.HarborSizeMapper;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.entity.HarborSize;
import com.test.hangyun.service.HarborSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 港口尺寸字典。字段没有唯一约束, 不查重; 删除前查引用(港口)。 */
@Service
@RequiredArgsConstructor
public class HarborSizeServiceImpl implements HarborSizeService {

    private final HarborSizeMapper harborSizeMapper;

    @Override
    public PageResult<HarborSizeVO> page(HarborSizeQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<HarborSize> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            w.likeRight(HarborSize::getSize, req.getKeyword().trim());
        }
        w.orderByAsc(HarborSize::getId);

        Page<HarborSize> p = harborSizeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, HarborSizeVO::from);
    }

    @Override
    public HarborSizeVO getById(Long id) {
        return HarborSizeVO.from(getExisting(id));
    }

    @Override
    public List<HarborSizeOptionVO> options() {
        return harborSizeMapper.selectList(
                        new LambdaQueryWrapper<HarborSize>().orderByAsc(HarborSize::getId))
                .stream().map(HarborSizeOptionVO::from).toList();
    }

    @Override
    @Transactional
    @OpLog(module = "港口尺寸", table = "harbor_size", type = OpType.INSERT, desc = "新增港口尺寸")
    public void create(HarborSizeReq req) {
        HarborSize e = new HarborSize();
        e.setSize(req.getSize().trim());
        harborSizeMapper.insert(e);
    }

    @Override
    @Transactional
    @OpLog(module = "港口尺寸", table = "harbor_size", type = OpType.UPDATE, desc = "修改港口尺寸")
    public void update(Long id, HarborSizeReq req) {
        getExisting(id);

        LambdaUpdateWrapper<HarborSize> u = new LambdaUpdateWrapper<>();
        u.eq(HarborSize::getId, id).set(HarborSize::getSize, req.getSize().trim());
        harborSizeMapper.update(null, u);
    }

    @Override
    @Transactional
    @OpLog(module = "港口尺寸", table = "harbor_size", type = OpType.DELETE, desc = "删除港口尺寸")
    public void delete(Long id) {
        getExisting(id);
        long ports = harborSizeMapper.countPortsByHarborSizeId(id);
        if (ports > 0) {
            throw BizException.conflict("该尺寸下有 " + ports + " 个港口, 无法删除");
        }
        harborSizeMapper.deleteById(id);
    }

    private HarborSize getExisting(Long id) {
        HarborSize e = harborSizeMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("港口尺寸不存在");
        }
        return e;
    }
}
