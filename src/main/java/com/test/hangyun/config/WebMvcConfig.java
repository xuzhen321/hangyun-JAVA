package com.test.hangyun.config;

import com.test.hangyun.auth.AuthInterceptor;
import com.test.hangyun.constant.DateTimeConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;

/**
 * Web 层配置: 跨域 + 登录令牌拦截 + URL 查询参数的类型转换。
 * <p>
 * 跨域: 开发期前端从任意地址访问后端(不同 IP、不同端口)都会跨域, 这里一律放行。
 * 用 {@code allowedOriginPatterns("*")} 而不是 {@code allowedOrigins("*")},
 * 后者在允许携带凭证时不被 Spring 接受。目前不放行凭证(不用 Cookie);
 * 前端 JWT 走 {@code Authorization} 请求头, 已由 {@code allowedHeaders("*")} 覆盖。
 * 生产上线前应收紧 {@code allowedOriginPatterns}, 改成真实前端域名。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                // 预检请求结果缓存 1 小时, 减少 OPTIONS 往返
                .maxAge(3600);
    }

    /**
     * 登录令牌拦截: 除白名单外, 所有接口都要带 {@code Authorization: Bearer <token>}。
     * <p>
     * 白名单里每一项的理由:
     * <ul>
     *   <li>{@code /auth/login} —— 登录本身当然不能要求先登录(否则死锁)</li>
     *   <li>Swagger 那几个路径 —— 不然打不开文档页, 而且它们会连带请求一堆静态资源</li>
     *   <li>{@code /error} —— Spring Boot 的错误转发入口。不放行的话, 一个 404 会被
     *       拦截器改写成 401, 排查问题时会被彻底带偏</li>
     * </ul>
     * <p>
     * ⚠️ {@code /auth/logout} 和 {@code /auth/me} **不在白名单里** —— 它们本来就要求已登录。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/error"
                );
    }

    /**
     * URL 查询参数里的时间转换, 例如
     * {@code ?qualificationValidToFrom=2027-01-01 00:00:00}。
     * <p>
     * JSON 请求体走的是 Jackson({@link JacksonConfig}), 查询参数走的却是 Spring 的类型转换,
     * 两条路径互不相干 —— 只配了 JacksonConfig 的话, 查询参数里的时间会解析失败。
     * 这里复用同一份格式定义, 保证两边接受的写法一致。
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    /** 解析 {@code yyyy-MM-dd HH:mm:ss} 与 {@code yyyy-MM-dd'T'HH:mm:ss} 两种写法 */
    private static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

        @Override
        public LocalDateTime convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            return LocalDateTime.parse(source.trim(), DateTimeConstants.IN_FORMATTER);
        }
    }
}
