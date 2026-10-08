package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Company;

/**
 * 公司信息表 company 。
 * <p>
 * 目前只服务于集装箱模块的引用校验和名称展示(箱主、操作方), 没有对应的 Controller/Service,
 * /companies 做成完整资源时再补。
 */
public interface CompanyMapper extends BaseMapper<Company> {
}
