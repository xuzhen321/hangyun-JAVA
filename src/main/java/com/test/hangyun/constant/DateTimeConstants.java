package com.test.hangyun.constant;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * 时间格式约定。
 * <p>
 * 输出只有一种, 输入允许两种 —— 这条规则同时管着两条互不相干的路径, 所以格式定义放这里共用:
 * <ul>
 *   <li>JSON 请求体/响应体: Jackson, 见 {@code config/JacksonConfig}</li>
 *   <li>URL 查询参数: Spring 类型转换, 见 {@code config/WebMvcConfig#addFormatters}</li>
 * </ul>
 * 两处各写一份的话, 迟早有一边被改漏。
 */
public final class DateTimeConstants {

    /** 输出格式: 不带 T、不带毫秒 */
    public static final DateTimeFormatter OUT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 输入格式: 两种写法都接受 */
    public static final DateTimeFormatter IN_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            .toFormatter();

    /** 工具类, 不允许实例化 */
    private DateTimeConstants() {
    }
}
