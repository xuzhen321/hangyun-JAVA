package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时区字典表 timezone 。被 port.timezone_id 引用。
 * <p>
 * ⚠️ **两个字段名都是带引号的大小写混合列名**（`"timezone_UTC8"`、
 * `"timezone_Asia_Shanghai"`），PostgreSQL 会保持大小写，所以必须用
 * {@code @TableField("\"...\"")} 显式写出带引号的列名 —— 和 ship_type 的 "GT"/"NT"/"DWT" 是同一个坑。
 * <p>
 * 两列在库里**都有唯一约束**。
 */
@Data
@TableName("timezone")
public class Timezone {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 港口时区。库里列名是 "timezone_UTC8" */
    @TableField("\"timezone_UTC8\"")
    private String timezoneUtc8;

    /** 港口时区(+8)。库里列名是 "timezone_Asia_Shanghai" */
    @TableField("\"timezone_Asia_Shanghai\"")
    private String timezoneAsiaShanghai;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
