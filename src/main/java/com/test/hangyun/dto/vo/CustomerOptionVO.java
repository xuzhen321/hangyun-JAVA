package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Customer;
import lombok.Data;

/**
 * 客户下拉框候选项。
 * <p>
 * 只带够用的三个字段: 管理员靠姓名认出是谁, 靠电话区分同名的人, 提交时用 id。
 * 其余字段(email/地址/资质/状态…)在这里没有意义, 不要往这个类上加。
 */
@Data
public class CustomerOptionVO {

    /** 提交订单时要用这个 id */
    private Long id;

    /** 客户姓名, 下拉框显示的主文本 */
    private String name;

    /** 联系方式, 用来区分同名客户 */
    private String phone;

    public static CustomerOptionVO from(Customer e) {
        if (e == null) {
            return null;
        }
        CustomerOptionVO vo = new CustomerOptionVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setPhone(e.getPhone());
        return vo;
    }
}
