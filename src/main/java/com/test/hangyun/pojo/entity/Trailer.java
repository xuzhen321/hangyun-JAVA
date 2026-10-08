package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拖车信息表 trailer 。
 * <p>
 * 报告中的关系模式: Trailer(no, phone, name)
 * 主键是**拖车号**(varchar(30)), 由业务方提供, 不是后端生成的, 所以用 {@link IdType#INPUT}。
 * 被 container_trailer_record.track_no 逻辑引用(提空箱拖车号)。
 */
@Data
@TableName("trailer")
public class Trailer {

    /** 拖车号, 业务主键, 由前端提供 */
    @TableId(value = "no", type = IdType.INPUT)
    private String no;

    /** 联系电话 */
    private String phone;

    /** 司机姓名 */
    private String name;

    /**
     * 创建时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime insertTime;

    /**
     * 修改时间。
     * 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可。
     */
    private LocalDateTime updateTime;
}
