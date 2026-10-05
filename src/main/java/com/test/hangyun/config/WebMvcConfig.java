package com.test.hangyun.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置。
 * <p>
 * 开发期前端从任意地址访问后端(不同 IP、不同端口)都会跨域, 这里一律放行。
 * 用 {@code allowedOriginPatterns("*")} 而不是 {@code allowedOrigins("*")},
 * 后者在允许携带凭证时不被 Spring 接受。
 * <p>
 * 目前不放行凭证(不用 Cookie); 前端 JWT 走 {@code Authorization} 请求头,
 * 已由 {@code allowedHeaders("*")} 覆盖, 不受此开关影响。
 * <p>
 * 生产上线前应收紧 {@code allowedOriginPatterns}, 改成真实前端域名。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

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
}
