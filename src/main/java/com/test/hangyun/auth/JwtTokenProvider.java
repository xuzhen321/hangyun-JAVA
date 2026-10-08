package com.test.hangyun.auth;

import com.test.hangyun.constant.AuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 的签发与校验。
 * <p>
 * ⚠️ 这里用的是 **jjwt 0.12.x 的 API**, 和网上大量 0.11.x 的示例**完全不兼容**
 * (0.11 的 {@code setClaims/parseClaimsJws} 这套已经废弃)。主要差别:
 * <ul>
 *   <li>签发: {@code Jwts.builder().subject(..).claim(..).expiration(..).signWith(key).compact()}</li>
 *   <li>校验: {@code Jwts.parser().verifyWith(key).build().parseSignedClaims(token)}</li>
 * </ul>
 * <p>
 * 令牌是无状态的: 服务端**不存**任何会话, 只靠签名和过期时间判断。所以登出只能由前端
 * 丢掉令牌来实现(见 {@code AuthServiceImpl#logout})。
 */
@Component
public class JwtTokenProvider {

    /** HMAC-SHA256 的密钥。长度必须 >= 32 字节, 否则 signWith 时会抛 WeakKeyException */
    private final SecretKey key;

    /** 令牌有效期(毫秒) */
    private final long expireMillis;

    public JwtTokenProvider(@Value("${hangyun.jwt.secret}") String secret,
                            @Value("${hangyun.jwt.expire-minutes}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60_000L;
    }

    /** 签发令牌。payload 里放 userId 和 username(设计文档 6.4 的最低要求) */
    public String issue(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(AuthConstants.CLAIM_USER_ID, userId)
                .claim(AuthConstants.CLAIM_USERNAME, username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 校验并解析令牌。
     *
     * @return 解析成功返回用户身份; **签名不对 / 格式不对 / 已过期都返回 null**
     *         —— 三种情况故意不区分, 调用方统一按"未登录"处理
     */
    public CurrentUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object rawId = claims.get(AuthConstants.CLAIM_USER_ID);
            String username = claims.get(AuthConstants.CLAIM_USERNAME, String.class);
            if (rawId == null || username == null) {
                return null;
            }
            // 不直接 get(.., Long.class): jjwt 按 JSON 数字的大小反序列化,
            // 小数值会变成 Integer, 直接要 Long 会抛 RequiredTypeException。
            // 走 toString 再转, 两种都能接住。
            return new CurrentUser(Long.valueOf(rawId.toString()), username);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** 令牌有效期(毫秒), 登录响应里回给前端, 方便它做提前续期或到期提醒 */
    public long getExpireMillis() {
        return expireMillis;
    }
}
