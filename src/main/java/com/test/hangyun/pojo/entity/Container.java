package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱信息表 container （写用）。
 * <p>
 * 报告中的关系模式: Container(no, type_id, owner_id, operator_id, seal_no, status_id,
 *                            danger_flag, maritime_flag, carrier_operate, ctr_status_terminal)
 * <p>
 * 主键是**箱号**(varchar(30)), 由业务方提供(如 SEGU9481570), 不是后端生成的,
 * 所以用 {@code IdType.INPUT} —— MyBatis-Plus 不做任何生成。
 * <p>
 * 两个 char(1) 标志位在实体里保持原样存 '0'/'1'(库里就是这么存的),
 * 但对前端暴露成 Boolean, 转换在 Service 层做 —— 见 {@link com.test.hangyun.dto.vo.ContainerVO}。
 */
@Data
@TableName("container")
public class Container {

    /** 箱号, 业务主键, 由前端提供 */
    @TableId(value = "no", type = IdType.INPUT)
    private String no;

    /** 箱型ID, 逻辑外键 -> container_type.id */
    private Long typeId;

    /** 箱主ID, 逻辑外键 -> company.id */
    private Long ownerId;

    /** 操作方ID, 逻辑外键 -> company.id */
    private Long operatorId;

    /** 铅封号 */
    private String sealNo;

    /** 当前状态ID, 逻辑外键 -> container_status.id */
    private Long statusId;

    /** 危险品标识: '0' 否, '1' 是 */
    private String dangerFlag;

    /** 海事标识: '0' 否, '1' 是 */
    private String maritimeFlag;

    /** 船公司操作: 加锁、截关等 */
    private String carrierOperate;

    /** 集装箱堆场状态, 特指在堆场或码头终端的状态 */
    private String ctrStatusTerminal;

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
