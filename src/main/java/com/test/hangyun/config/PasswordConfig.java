package com.test.hangyun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码哈希器。
 * <p>
 * 只引入了 {@code spring-security-crypto} 这一个模块(不是整个 spring-boot-starter-security),
 * 所以这里没有过滤器链、没有自动登录页 —— 只有 BCrypt 这一个工具类。
 * <p>
 * 做成 Bean 是为了**全局只用一个实例**: BCryptPasswordEncoder 是无状态的, 但每次 new
 * 都会重新解析强度参数; 更要紧的是 {@code matches()} 必须和 {@code encode()} 用同一套策略。
 * <p>
 * BCrypt 的哈希里**自带盐和强度**(形如 {@code $2a$10$...}), 所以库里存的就是完整哈希,
 * 不需要额外存 salt 列, 校验时直接把明文和哈希一起交给 {@link PasswordEncoder#matches} 即可。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 强度 10 是 BCryptPasswordEncoder 的默认值, 显式写出来省得以后有人以为是随手设的
        return new BCryptPasswordEncoder(10);
    }
}
