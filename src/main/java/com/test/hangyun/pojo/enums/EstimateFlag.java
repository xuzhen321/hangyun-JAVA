package com.test.hangyun.pojo.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 事件时间是"实际发生"还是"预计发生"。
 * <p>
 * 对应 container_event.is_esti ({@code char(1)})。**注意映射方向**:
 * <pre>
 *   N = 实际发生   ← 不是"否"的意思, 别按字面理解
 *   Y = 预计发生
 * </pre>
 * 这条来自周报原文("N实际发生，Y预计发生"), 第四周报告和第六周报告写的一致,
 * initial.sql 里 is_esti 的列注释也是这么写的。
 * <p>
 * 对前端同样**直接给中文文字**("实际" / "预计"), 不给 'Y'/'N'。
 */
@Getter
public enum EstimateFlag implements IEnum<String> {

    /** N 实际发生 */
    ACTUAL("N", "实际"),

    /** Y 预计发生 */
    ESTIMATED("Y", "预计");

    /** 存进数据库的值 */
    private final String code;

    /** 返回给前端的文字 */
    private final String text;

    EstimateFlag(String code, String text) {
        this.code = code;
        this.text = text;
    }

    /** MyBatis-Plus 用它把枚举写成库里的 'N'/'Y', 也用它把库里的值读回枚举 */
    @Override
    public String getValue() {
        return code;
    }

    /** 序列化成 JSON 时用这个: 前端看到的是 "实际" / "预计" */
    @JsonValue
    public String getText() {
        return text;
    }

    /** 从 JSON 反序列化: 前端提交的也必须是 "实际" / "预计" */
    @JsonCreator
    public static EstimateFlag fromText(String text) {
        for (EstimateFlag v : values()) {
            if (v.text.equals(text)) {
                return v;
            }
        }
        throw new IllegalArgumentException(
                "是否预计只能是 \"实际\" 或 \"预计\", 收到: " + text);
    }
}
