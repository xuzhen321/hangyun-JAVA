package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.User;

/**
 * 系统用户表 users 的写操作。
 * <p>
 * 查询走视图 v_user（见 {@link UserViewMapper}）。
 * <p>
 * 没有自定义 SQL: 唯一性查重(username)用 {@code selectCount} + 条件构造器就够了。
 * <p>
 * ⚠️ **删除前不需要查引用**: 用户是逻辑删除, 行还在, {@code Log.user_id} 不会悬空
 * (设计文档 3.3: 逻辑删除的表不需要引用检查)。
 */
public interface UserMapper extends BaseMapper<User> {
}
