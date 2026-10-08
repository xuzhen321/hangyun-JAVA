package com.test.hangyun.auth;

import com.test.hangyun.common.Result;
import com.test.hangyun.constant.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 登录令牌校验。
 * <p>
 * 白名单(登录接口、Swagger、错误页)在 {@code WebMvcConfig#addInterceptors} 里用
 * {@code excludePathPatterns} 排除, **不在这里判断路径** —— 路径匹配交给 Spring 更准。
 * <p>
 * 校验通过后把用户身份放进 {@link UserContext}, 整条请求链(尤其是日志切面)从那里取。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // CORS 预检直接放过: 浏览器发 OPTIONS 时**不会带 Authorization 头**
        // (只带 Origin / Access-Control-Request-*), 拦下来前端一个请求都发不出去。
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(AuthConstants.HEADER);
        if (header == null || !header.startsWith(AuthConstants.BEARER_PREFIX)) {
            return reject(response);
        }
        String token = header.substring(AuthConstants.BEARER_PREFIX.length()).trim();
        CurrentUser user = tokenProvider.parse(token);
        if (user == null) {
            return reject(response);
        }

        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // ⚠️ 成功、失败、抛异常都会走到这里, 是清 ThreadLocal 的唯一可靠位置。
        //    Tomcat 的工作线程是复用的, 不清的话下一个请求会读到上一个用户的身份。
        UserContext.clear();
    }

    /**
     * 写 401 响应。
     * <p>
     * 为什么不用全局异常处理器: 拦截器在 {@code DispatcherServlet} 的常规流程之外执行,
     * 在这里 {@code throw} 出去的异常**不会被** {@code @RestControllerAdvice} 接住,
     * 只能自己把响应写出去。响应体的结构靠 {@link Result} 保证和别处一致。
     */
    private boolean reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.error(AuthConstants.MSG_UNAUTHORIZED)));
        return false;
    }
}
