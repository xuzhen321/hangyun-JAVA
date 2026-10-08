package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.view.UserView;

/**
 * 系统用户视图 v_user 的只读查询。
 * <p>
 * ⚠️ 视图查询**不受 {@code @TableLogic} 影响**, 逻辑删除的行(status='2')要由调用方
 * 自己用条件构造器过滤掉 —— 见 {@code UserServiceImpl#page}。
 */
public interface UserViewMapper extends BaseMapper<UserView> {
}
