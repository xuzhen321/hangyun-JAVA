package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.User;
import com.test.hangyun.pojo.view.UserView;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应对象。
 * <p>
 * ⚠️ **没有密码字段, 这是刻意的** —— 列表、详情都不允许带出密码(哪怕是哈希)。
 * 视图 {@code UserView} 里有 {@code passwordHash}, 但这里**故意不映射**它,
 * 导出列定义里也没有它。给认证逻辑用的密码只走 Mapper 直查, 不经过 VO。
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 登录名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 电话 */
    private String phone;

    /** 账号状态: 0正常, 1冻结, 2已删除 */
    private String status;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static UserVO from(UserView v) {
        if (v == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(v.getUserId());
        vo.setUsername(v.getUsername());
        vo.setRealName(v.getRealName());
        vo.setPhone(v.getPhone());
        vo.setStatus(v.getStatus());
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        // ⚠️ 刻意不 set 密码: VO 上没有这个字段
        return vo;
    }

    /**
     * 由**基表实体**构造 —— 登录时手里拿到的是 {@code User}(基表才有密码哈希可用),
     * 不为了一次响应再去查一遍视图。
     */
    public static UserVO from(User e) {
        if (e == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(e.getId());
        vo.setUsername(e.getUsername());
        vo.setRealName(e.getRealName());
        vo.setPhone(e.getPhone());
        vo.setStatus(e.getStatus());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        // ⚠️ 同样刻意不 set 密码
        return vo;
    }
}
