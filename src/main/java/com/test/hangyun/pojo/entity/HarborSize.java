package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 港口尺寸字典表 harbor_size 。被 port.harbor_size_id 引用。字段没有唯一约束。
 */
@Data
@TableName("harbor_size")
public class HarborSize {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 港口尺寸 */
    private String size;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
