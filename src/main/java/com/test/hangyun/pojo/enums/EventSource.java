package com.test.hangyun.pojo.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 事件数据来源。
 * <p>
 * 库里 container_event.source 是 {@code char(1)}, 存 '1'/'0'; 但对前端**直接给中文文字**,
 * 不让前端碰编码(文档 3.5 的要求)。
 * <ul>
 *   <li>{@code code} 对数据库, 由 MyBatis-Plus 的 {@link IEnum} 自动转换</li>
 *   <li>{@code text} 对前端, 由 Jackson 的 {@code @JsonValue} / {@code @JsonCreator} 转换</li>
 * </ul>
 * 两边的对应关系与 initial.sql 里 source 的列注释一致: 1船公司, 0港区。
 *
 * @see EstimateFlag 同类的另一个标志位
 */
@Getter
public enum EventSource implements IEnum<String> {

    /** 1 船公司 */
    CARRIER("1", "船公司"),

    /** 0 港区 */
    PORT("0", "港区");

    /** 存进数据库的值 */
    private final String code;

    /** 返回给前端的文字 */
    private final String text;

    EventSource(String code, String text) {
        this.code = code;
        this.text = text;
    }

    /** MyBatis-Plus 用它把枚举写成库里的 '1'/'0', 也用它把库里的值读回枚举 */
    @Override
    public String getValue() {
        return code;
    }

    /** 序列化成 JSON 时用这个: 前端看到的是 "船公司" / "港区", 不是 '1'/'0' */
    @JsonValue
    public String getText() {
        return text;
    }

    /** 从 JSON 反序列化: 前端提交的也必须是 "船公司" / "港区" */
    @JsonCreator
    public static EventSource fromText(String text) {
        for (EventSource v : values()) {
            if (v.text.equals(text)) {
                return v;
            }
        }
        throw new IllegalArgumentException(
                "事件来源只能是 \"船公司\" 或 \"港区\", 收到: " + text);
    }
}
