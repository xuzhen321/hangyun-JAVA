package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.view.CustomerView;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户响应对象。
 * 与视图对象分开, 保证接口契约不随视图列变化而波动。
 */
@Data
public class CustomerVO {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String qualification;
    private LocalDateTime qualificationValidTo;

    /** 客户状态ID */
    private Long status;

    /** 客户状态中文描述 */
    private String statusDescription;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static CustomerVO from(CustomerView v) {
        if (v == null) {
            return null;
        }
        CustomerVO vo = new CustomerVO();
        vo.setId(v.getId());
        vo.setName(v.getName());
        vo.setPhone(v.getPhone());
        vo.setEmail(v.getEmail());
        vo.setAddress(v.getAddress());
        vo.setQualification(v.getQualification());
        vo.setQualificationValidTo(v.getQualificationValidTo());
        vo.setStatus(v.getStatus());
        vo.setStatusDescription(v.getStatusDescription());
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        return vo;
    }
}
