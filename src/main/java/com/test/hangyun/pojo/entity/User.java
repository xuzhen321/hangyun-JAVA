package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户表 users （写用）。
 * <p>
 * ⚠️ 建表语句写的是 {@code create table Users}, 但 PostgreSQL 会把**不加引号的标识符折成小写**,
 * 所以真实表名是 {@code users} —— 和 Customer/Port/Orders 一个道理。
 * <p>
 * 关系模式: Users(id, username, password, real_name, phone, status)
 * 主键 id **自增**(identity), 由数据库生成, 插入后回填到实体上。
 * <p>
 * 删除是**逻辑删除**: 只把 status 置成 '2'(已删除), 行还在, 见 {@link com.test.hangyun.constant.UserConstants}。
 * <p>
 * ⚠️ {@code password} 存的是 **BCrypt 哈希, 不是明文**, 且一律不出现在任何响应里
 * (列表/详情/导出都排除, 见 {@code UserVO} 和导出列定义)。
 */
@Data
@TableName("users")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 登录名。库里有唯一约束 */
    private String username;

    /** 密码哈希(BCrypt)。**不是明文, 也不允许出现在响应里** */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 电话 */
    private String phone;

    /** 账号状态: 0正常, 1冻结, 2已删除(逻辑删除的落点) */
    private String status;

    /** 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可 */
    private LocalDateTime insertTime;

    /** 由数据库触发器 set_time_fields() 维护, 应用层一律不要赋值, 留 null 即可 */
    private LocalDateTime updateTime;
}
