package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.CountryQueryReq;
import com.test.hangyun.dto.CountryReq;
import com.test.hangyun.dto.vo.CountryOptionVO;
import com.test.hangyun.dto.vo.CountryVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.CountryMapper;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.entity.Country;
import com.test.hangyun.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 国家字典维护。
 * <p>
 * 被两处引用: vessel.country_id(船旗国)、port.country_id(港口所在国家) —— 删除前两处都要查。
 * country_code 有唯一约束, 所以新增/修改要查重。
 */
@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryMapper countryMapper;

    @Override
    public PageResult<CountryVO> page(CountryQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<Country> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, req.getKeyword());
        w.orderByAsc(Country::getId);

        Page<Country> p = countryMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, CountryVO::from);
    }

    @Override
    public CountryVO getById(Long id) {
        return CountryVO.from(getExisting(id));
    }

    @Override
    public List<CountryOptionVO> options(String keyword) {
        LambdaQueryWrapper<Country> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, keyword);
        w.orderByAsc(Country::getId);

        // searchCount=false: 下拉框不需要 total
        return countryMapper.selectPage(new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(CountryOptionVO::from).toList();
    }

    @Override
    @Transactional
    @OpLog(module = "国家字典", table = "country", type = OpType.INSERT, desc = "新增国家")
    public void create(CountryReq req) {
        String code = req.getCountryCode().trim();
        ensureCodeUnique(code, null);

        Country e = new Country();
        e.setCountryCode(code);
        e.setCountryCnname(trimToNull(req.getCountryCnname()));
        e.setCountryEnname(trimToNull(req.getCountryEnname()));
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        countryMapper.insert(e);
    }

    @Override
    @Transactional
    @OpLog(module = "国家字典", table = "country", type = OpType.UPDATE, desc = "修改国家")
    public void update(Long id, CountryReq req) {
        getExisting(id);
        String code = req.getCountryCode().trim();
        // 查重时排除自己, 否则"只改中文名、代码不变"的提交会被误判为重复
        ensureCodeUnique(code, id);

        LambdaUpdateWrapper<Country> u = new LambdaUpdateWrapper<>();
        u.eq(Country::getId, id)
                .set(Country::getCountryCode, code)
                .set(Country::getCountryCnname, trimToNull(req.getCountryCnname()))
                .set(Country::getCountryEnname, trimToNull(req.getCountryEnname()));
        countryMapper.update(null, u);
    }

    @Override
    @Transactional
    @OpLog(module = "国家字典", table = "country", type = OpType.DELETE, desc = "删除国家")
    public void delete(Long id) {
        getExisting(id);

        // 删除保护: 船舶(船旗国)和港口(所在国家)两处引用都要查
        long refs = countryMapper.countReferences(id);
        if (refs > 0) {
            throw BizException.conflict("该国家被 " + refs + " 条记录引用(船舶船旗国或港口所在国家), 无法删除");
        }
        countryMapper.deleteById(id);
    }

    private Country getExisting(Long id) {
        Country e = countryMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("国家不存在");
        }
        return e;
    }

    /** country_code 有唯一约束, 提前查重以便返回 409 而不是撞唯一键报 500 */
    private void ensureCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<Country> w = new LambdaQueryWrapper<>();
        w.eq(Country::getCountryCode, code);
        w.ne(excludeId != null, Country::getId, excludeId);
        if (countryMapper.selectCount(w) > 0) {
            throw BizException.conflict("国家代码已存在: " + code);
        }
    }

    /**
     * 关键字同时匹配**代码**、**中文名**、**英文名**的前缀, 任一命中即可。
     * 用 and(...) 把三个 OR 包起来, 否则将来再加别的条件时会被 OR 拆散、绕过过滤。
     */
    private void applyKeywordFilter(LambdaQueryWrapper<Country> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(Country::getCountryCode, kw)
                .or().likeRight(Country::getCountryCnname, kw)
                .or().likeRight(Country::getCountryEnname, kw));
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
