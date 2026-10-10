package com.test.hangyun.log;

import com.test.hangyun.auth.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 把"当前操作人"告诉数据库, 供操作日志触发器 {@code log_row_change()} 取用。
 * <p>
 * ⚠️ 为什么需要它:
 * UPDATE / DELETE 的 {@code before_value} 改由**数据库触发器**记录(它能拿到 OLD 整行),
 * 触发器跑在库里, 根本不知道 HTTP 请求是谁发的。所以要在**同一个事务**里先写一个
 * 事务局部变量: {@code set_config('app.user_id', <id>, true)}, 触发器再读
 * {@code current_setting('app.user_id', true)} 填进 {@code log.user_id}。
 * <p>
 * ⚠️ 为什么拦 MyBatis 的 {@code Executor.update}, 而不是放在 {@link com.test.hangyun.auth.AuthInterceptor} 里:
 * <ul>
 *   <li>{@code AuthInterceptor} 是 Spring MVC 拦截器, 跑在**事务外面** —— 那时
 *       {@code set_config(..., true)} 立刻失效, 等 UPDATE 执行时触发器什么也读不到。</li>
 *   <li>拦截 {@code Executor.update} 只在 **INSERT / UPDATE / DELETE** 时触发(SELECT 不拦),
 *       而且正好落在事务内、和业务写操作同一条连接。</li>
 * </ul>
 * <p>
 * ⚠️ 第三个参数必须是 {@code true}(事务级)。用 {@code false} 是会话级的, Tomcat 复用连接时
 * 会把上一个请求的用户留给下一个请求 —— 和 {@link UserContext} 那个 ThreadLocal 坑一模一样。
 * <p>
 * ⚠️ 由此带来一个前提: 写方法必须带 {@code @Transactional}, 否则 {@code set_config} 所在
 * 的那条隐式事务一结束就失效。项目里所有写方法都有, 见各 ServiceImpl。
 * <p>
 * ⚠️ 不抛异常: 拿不到操作人顶多让日志的 user_id 为空, 绝不能因此让业务写失败。
 */
@Slf4j
@Component
@Intercepts(@Signature(type = Executor.class, method = "update",
        args = {MappedStatement.class, Object.class}))
public class AuditUserInterceptor implements Interceptor {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 直接 new 一个 JdbcTemplate, 不依赖 Spring Boot 是否自动配置了 JdbcTemplate Bean。
     * 它内部走 DataSourceUtils, 会拿到**事务绑定的那条连接**, 所以和随后的 UPDATE 是同一个事务。
     */
    public AuditUserInterceptor(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Long userId = UserContext.currentUserId();
        if (userId != null) {
            try {
                // is_local = true: 事务提交/回滚后自动消失, 不会留在池化连接上
                jdbcTemplate.queryForObject(
                        "select set_config('app.user_id', ?, true)",
                        String.class, userId.toString());
            } catch (Exception e) {
                log.warn("写入 app.user_id 失败, 触发器记的日志将没有操作人", e);
            }
        }
        // 未登录(白名单路径)时不写 —— 事务局部变量本来是空的, 触发器自然读到 null
        return invocation.proceed();
    }
}
