package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户管理视图 v_customer （只读）。
 * 已经联好了 customer_status, 查询一次即可拿到状态描述, 无需前端二次联查。
 * <p>
 * 对应列: id, name, phone, email, address, qualification,
 *         qualification_valid_to, status, status_description, insert_time, update_time
 */
@Data
@TableName("v_customer")
public class CustomerView {


    /**
     * INPUT 等于告诉 MP："这个实体的 id 只会来自查询结果，别想着生成它"——这正是视图实体的正确语义。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String address;
    private String qualification;
    private LocalDateTime qualificationValidTo;

    /** 视图里是 customer.status_id, 别名 status */
    private Long status;

    /** 来自 customer_status.description */
    private String statusDescription;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
