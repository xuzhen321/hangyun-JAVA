package com.test.hangyun.dto;

import lombok.Data;

/**
 * 用户列表查询条件。
 * <p>
 * 每个条件都是可选的, 不传即不参与筛选, 传了才 AND 上去。
 * <p>
 * ⚠️ 不传 {@code status} 时, 已删除(status='2')的用户**不会出现在列表里**;
 * 显式传 {@code status=2} 才看得到(和客户列表一个口径)。
 */
@Data
public class UserQueryReq {

    /** 页码, 从 1 开始 */
    private Integer page = 1;

    /** 每页条数, 上限 100 */
    private Integer size = 20;

    /** 关键字, **前缀匹配**: 同时匹配**登录名**和**真实姓名**, 任一命中即返回 */
    private String keyword;

    /** 账号状态, 精确匹配: 0正常 / 1冻结 / 2已删除 */
    private String status;
}
