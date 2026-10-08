package com.test.hangyun.log;

import com.test.hangyun.auth.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * 操作日志切面: 凡是标了 {@link OpLog} 的方法, 调用情况都会记进 {@code log} 表。
 * <p>
 * 记录内容: 谁({@code user_id}, 取自 {@link UserContext})、什么操作({@code type_id})、
 * 动了哪张表哪条记录({@code target_table} / {@code target_id})、什么时候、
 * 成功还是失败、失败时报了什么错、以及提交的入参({@code after_value})。
 * <p>
 * ⚠️ **{@code before_value} 恒为 null**, 这是通用切面的能力边界, 不是漏写:
 * 要拿到"改之前"的快照, 必须知道目标表、按主键回查一次库。切面只有方法签名和入参,
 * 拿不到这些信息。真需要留痕"改前值"的地方, 得在那个 Service 里显式查一次再手工写日志。
 * 目前 {@code after_value}(提交的内容)+ 失败信息已经够支撑"谁在什么时候动了什么"。
 * <p>
 * ⚠️ 切面自己**不抛异常**: 记日志失败绝不能把已经成功的业务搞成失败(见
 * {@link OperationLogWriter#writeSafely})。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class LogAspect {

    /** SpEL 解析器是线程安全的, 建成静态的复用一份 */
    private static final ExpressionParser SPEL = new SpelExpressionParser();

    private final OperationLogWriter operationLogWriter;
    private final ObjectMapper objectMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        try {
            Object result = pjp.proceed();
            record(opLog, pjp, true, null);
            return result;
        } catch (Throwable e) {
            // 先记日志再原样抛出 —— 异常是业务自己要处理的, 切面只负责留痕, 不能改变行为
            record(opLog, pjp, false, e);
            throw e;
        }
    }

    private void record(OpLog opLog, ProceedingJoinPoint pjp, boolean success, Throwable error) {
        try {
            operationLogWriter.writeSafely(
                    UserContext.currentUserId(),
                    opLog.type(),
                    opLog.table(),
                    resolveTargetId(opLog, pjp),
                    serializeArgs(pjp.getArgs()),
                    success,
                    error == null ? null : error.getMessage());
        } catch (Exception e) {
            // writeSafely 内部已经吞了写库异常; 走到这里说明是解析参数/序列化出的问题,
            // 同样不能让业务受影响
            log.error("记录操作日志失败: module={}, desc={}", opLog.module(), opLog.desc(), e);
        }
    }

    /**
     * 取目标记录主键。
     * <p>
     * 注解里写了 {@code targetId} 就按 SpEL 求值(引用参数名, 如 {@code "#no"});
     * 没写就按约定取**第一个参数** —— 项目的写方法签名基本都是 {@code update(id, req)} /
     * {@code delete(id)} 这种形状, 第一个参数正好就是主键。
     */
    private String resolveTargetId(OpLog opLog, ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();

        if (StringUtils.hasText(opLog.targetId())) {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            String[] names = sig.getParameterNames();
            if (names != null) {
                for (int i = 0; i < names.length && i < args.length; i++) {
                    ctx.setVariable(names[i], args[i]);
                }
            }
            Expression expr = SPEL.parseExpression(opLog.targetId());
            Object value = expr.getValue(ctx);
            return value == null ? null : String.valueOf(value);
        }

        // 约定: 第一个参数若是"简单值"就当主键; 是 DTO 的话说明这个方法没有单一目标记录
        if (args.length == 0 || !isSimpleValue(args[0])) {
            return null;
        }
        return String.valueOf(args[0]);
    }

    private static boolean isSimpleValue(Object o) {
        return o instanceof CharSequence || o instanceof Number || o instanceof Character;
    }

    /**
     * 把方法入参序列化成 JSON 存进 {@code after_value}。
     * <p>
     * ⚠️ 密码这类字段靠 DTO 上的 {@code @JsonProperty(access = WRITE_ONLY)} 挡住 ——
     * 不挡的话明文密码会被原样写进审计日志。
     * <p>
     * 序列化失败(参数里有不可序列化的东西)不上报异常, 退化成 toString, 保证日志这条线不断。
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            log.warn("操作日志序列化入参失败, 退化为 toString", e);
            return java.util.Arrays.toString(args);
        }
    }
}
