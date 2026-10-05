package com.test.hangyun.config;

import com.test.hangyun.constant.DateTimeConstants;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

/**
 * 时间格式配置（只管 JSON 请求体/响应体）。
 * <p>
 * 输出: 统一 {@code yyyy-MM-dd HH:mm:ss}, 不带上 T 的 ISO 形式和微秒。
 * 输入: {@code yyyy-MM-dd HH:mm:ss} 和 {@code yyyy-MM-dd'T'HH:mm:ss} 两种都接受。
 * 具体的格式定义在 {@link DateTimeConstants}, 与 URL 查询参数那条路径共用。
 * <p>
 * URL 查询参数不走这里, 走 {@link WebMvcConfig#addFormatters}。
 * <p>
 * 注意 Spring Boot 4 用的是 Jackson 3 (包名 {@code tools.jackson}), 与 Jackson 2 的 API 完全不同:
 * java.time 支持已内置于 databind, 没有 JavaTimeModule 可注册, 定制钩子也换成了
 * {@link JsonMapperBuilderCustomizer}。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("localDateTimeModule");
            module.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(DateTimeConstants.OUT_FORMATTER));
            module.addDeserializer(LocalDateTime.class,
                    new LocalDateTimeDeserializer(DateTimeConstants.IN_FORMATTER));
            builder.addModule(module);
        };
    }
}
