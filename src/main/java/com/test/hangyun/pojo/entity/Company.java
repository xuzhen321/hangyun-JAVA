package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 公司信息表 company 。
 * <p>
 * 箱主、操作方、船东、管理公司共用这一张字典表。
 * 目前只服务于集装箱模块的**引用校验**和**名称展示**, 所以只映射了这几列。
 * /companies 做成完整资源时再补全字段。
 */
@Data
@TableName("company")
public class Company {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 公司名称 */
    private String name;

    /** 公司代码: 如 CSLU */
    private String code;
}
