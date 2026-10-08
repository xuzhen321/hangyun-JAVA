package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.User;
import lombok.Data;

/**
 * 用户下拉框候选项。
 * <p>
 * 只带够用的三个字段: 显示登录名, 用真实姓名帮助认人, 提交时用 id。
 * <p>
 * ⚠️ **绝不含密码**, 连哈希也不行。
 */
@Data
public class UserOptionVO {

    /** 提交时要用这个 id */
    private Long id;

    /** 登录名, 下拉框显示的主文本 */
    private String username;

    /** 真实姓名, 用来区分登录名相近的人 */
    private String realName;

    public static UserOptionVO from(User e) {
        if (e == null) {
            return null;
        }
        UserOptionVO vo = new UserOptionVO();
        vo.setId(e.getId());
        vo.setUsername(e.getUsername());
        vo.setRealName(e.getRealName());
        return vo;
    }
}
