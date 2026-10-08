package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Company;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 公司信息表 company 的写操作 + 引用检查。
 * <p>
 * 这张表是**多用途字典**: 箱主、操作方、船东、管理公司都指向它。
 */
public interface CompanyMapper extends BaseMapper<Company> {

    /**
     * 统计有多少个集装箱把这个公司当作箱主或操作方。
     * <p>
     * container.owner_id 和 container.operator_id 都逻辑引用 company.id, 所以两个都要查 ——
     * 只查一个的话, "被当作操作方引用着"的公司会被误删。
     * <p>
     * 库里没有物理外键, 删除公司前必须由应用层自己检查引用。
     * <p>
     * ⚠️ 将来做 vessel 模块时, 这里还要加上 vessel.owner_company_id / manager_company_id。
     */
    @Select("select count(*) from container where owner_id = #{companyId} or operator_id = #{companyId}")
    long countContainersByCompanyId(@Param("companyId") Long companyId);
}
