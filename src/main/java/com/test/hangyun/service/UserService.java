package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.UserBatchDeleteReq;
import com.test.hangyun.dto.UserCreateReq;
import com.test.hangyun.dto.UserPasswordReq;
import com.test.hangyun.dto.UserQueryReq;
import com.test.hangyun.dto.UserUpdateReq;
import com.test.hangyun.dto.vo.UserOptionVO;
import com.test.hangyun.dto.vo.UserVO;

import java.util.List;

/**
 * 系统用户。完整的资源(列表/详情/增删改) + 重置密码 + 下拉框。
 * <p>
 * 删除是**逻辑删除**(status 置 '2'), 行还在。
 */
public interface UserService {

    /** 分页查询, 支持按登录名/真实姓名前缀搜索、按状态筛选 */
    PageResult<UserVO> page(UserQueryReq req);

    /** 按查询条件取不分页的全量列表, 供导出用。超过上限抛 400 */
    List<UserVO> listForExport(UserQueryReq req);

    /** 详情 */
    UserVO getById(Long id);

    /** 新增。密码明文进来, 哈希后落库 */
    void create(UserCreateReq req);

    /** 修改。**不含密码**(见 UserUpdateReq) */
    void update(Long id, UserUpdateReq req);

    /** 重置密码 */
    void resetPassword(Long id, UserPasswordReq req);

    /** 逻辑删除: 把 status 置为 '2', 行还在 */
    void delete(Long id);

    /** 批量逻辑删除。内置管理员在批次里则整批拒绝 */
    void deleteBatch(List<Long> ids);

    /** 下拉框候选: 最多 20 条, 排除已删除的 */
    List<UserOptionVO> options(String keyword);
}
