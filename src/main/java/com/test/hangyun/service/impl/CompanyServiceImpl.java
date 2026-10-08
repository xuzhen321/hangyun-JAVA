package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CompanyQueryReq;
import com.test.hangyun.dto.CompanyReq;
import com.test.hangyun.dto.vo.CompanyOptionVO;
import com.test.hangyun.dto.vo.CompanyVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.CompanyMapper;
import com.test.hangyun.pojo.entity.Company;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 公司信息维护。
 * <p>
 * 这张表是**多用途字典**: 箱主、操作方、船东、管理公司都指向它, 目前被集装箱引用。
 * name 和 code 在库里都没有唯一约束, 所以**不用查重**; 但删除前必须查引用。
 */
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyMapper companyMapper;

    @Override
    public PageResult<CompanyVO> page(CompanyQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Company> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, req.getKeyword());
        w.orderByAsc(Company::getId);

        Page<Company> p = companyMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, CompanyVO::from);
    }

    @Override
    public CompanyVO getById(Long id) {
        return CompanyVO.from(getExisting(id));
    }

    @Override
    public List<CompanyOptionVO> options(String keyword) {
        LambdaQueryWrapper<Company> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, keyword);
        w.orderByAsc(Company::getId);

        // searchCount=false: 下拉框不需要 total, 省掉那条 COUNT
        return companyMapper.selectPage(
                        new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(CompanyOptionVO::from).toList();
    }

    @OpLog(module = "公司信息", table = "company", type = OpType.INSERT, desc = "新增公司")
    @Override
    @Transactional
    public void create(CompanyReq req) {
        Company e = new Company();
        e.setName(req.getName().trim());
        e.setCode(trimToNull(req.getCode()));
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        companyMapper.insert(e);
    }

    @OpLog(module = "公司信息", table = "company", type = OpType.UPDATE, desc = "修改公司")
    @Override
    @Transactional
    public void update(Long id, CompanyReq req) {
        getExisting(id);

        LambdaUpdateWrapper<Company> u = new LambdaUpdateWrapper<>();
        u.eq(Company::getId, id)
                .set(Company::getName, req.getName().trim())
                .set(Company::getCode, trimToNull(req.getCode()));
        companyMapper.update(null, u);
    }

    @OpLog(module = "公司信息", table = "company", type = OpType.DELETE, desc = "删除公司")
    @Override
    @Transactional
    public void delete(Long id) {
        getExisting(id);

        // 删除保护: 库里没有外键约束, 必须自己挡住仍被引用的公司。
        // 注意箱主和操作方两个字段都要查 —— 只查一个会漏掉"被当作操作方引用着"的公司。
        long containers = companyMapper.countContainersByCompanyId(id);
        if (containers > 0) {
            throw BizException.conflict("该公司被 " + containers + " 个集装箱引用(箱主或操作方), 无法删除");
        }
        companyMapper.deleteById(id);
    }

    private Company getExisting(Long id) {
        Company e = companyMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("公司不存在");
        }
        return e;
    }

    /**
     * 关键字同时匹配**名称**和**代码**的前缀, 任一命中即可。
     * 用 and(...) 把两个 OR 包起来, 否则将来再加别的条件时会被 OR 拆散、绕过过滤。
     */
    private void applyKeywordFilter(LambdaQueryWrapper<Company> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(Company::getName, kw).or().likeRight(Company::getCode, kw));
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
