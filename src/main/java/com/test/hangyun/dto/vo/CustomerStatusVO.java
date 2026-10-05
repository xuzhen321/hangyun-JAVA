package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.CustomerStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户状态响应对象。
 * 与实体分开, 保证接口契约不随表结构变化而波动。
 */
@Data
public class CustomerStatusVO {

    private Long id;

    /** 状态描述: 1正常, 2异常, 3注销, 其他自定义 */
    private String description;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static CustomerStatusVO from(CustomerStatus e) {
        if (e == null) {
            return null;
        }
        CustomerStatusVO vo = new CustomerStatusVO();
        vo.setId(e.getId());
        vo.setDescription(e.getDescription());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
