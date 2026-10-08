package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户视图 v_user （只读）。
 * <p>
 * ⚠️ 视图里主键列叫 {@code user_id}(不是 id), 密码列叫 {@code password_hash}(不是 password)。
 * <p>
 * ⚠️ {@code @TableLogic} 对**视图查询无效** —— 用户是逻辑删除, 所有查 v_user 的地方
 * 都必须手工带上 {@code status <> '2'}, 见 {@code UserServiceImpl}。
 */
@Data
@TableName("v_user")
public class UserView {

    /** 视图里主键列叫 user_id */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 登录名 */
    private String username;

    /**
     * 密码哈希。
     * <p>
     * ⚠️ **只给认证逻辑用**(登录时比对密码), 绝不能进任何 VO —— 列表、详情、导出都不带它。
     */
    private String passwordHash;

    /** 真实姓名 */
    private String realName;

    /** 电话 */
    private String phone;

    /** 账号状态: 0正常, 1冻结, 2已删除 */
    private String status;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
