package com.test.hangyun.log;

import com.test.hangyun.constant.OperationLogConstants;
import com.test.hangyun.mapper.OperationLogMapper;
import com.test.hangyun.pojo.entity.OperationLog;
import com.test.hangyun.pojo.enums.OpType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 真正把日志写进库的地方 —— **独立于调用方的那个方法存在**。
 * <p>
 * ⚠️ 为什么单独一个 Bean, 而不是在切面里直接写:
 * {@code @Transactional} 靠 Spring 的**代理**生效, 同一个类内部自调用不走代理,
 * 注解形同虚设。切面直接写的话就落进业务事务里了, 业务一回滚日志也没了 ——
 * 偏偏最需要留下的就是那些失败的操作。
 * <p>
 * ⚠️ {@code REQUIRES_NEW}: 挂起外层事务, 自己开一个新事务并**独立提交**。
 * 这样业务回滚不影响日志; 反过来, 日志写失败也**不应该**把已经成功的业务拖回滚,
 * 所以 {@link #writeSafely} 把异常吞掉只记日志。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogWriter {

    private final OperationLogMapper operationLogMapper;

    /** 写日志。独立事务 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(Long userId, OpType type, String targetTable, String targetId,
                      String afterValue, boolean success, String errorMessage) {
        OperationLog e = new OperationLog();
        e.setUserId(userId);
        e.setTypeId(type.getId());
        // operation_time 是业务时间(操作发生的时刻), 由应用层显式赋值;
        // insert_time / update_time 才是触发器管的那两个, 这里不碰
        e.setOperationTime(LocalDateTime.now());
        e.setResultStatus(success
                ? OperationLogConstants.RESULT_SUCCESS : OperationLogConstants.RESULT_FAIL);
        e.setErrorMessage(truncate(errorMessage, OperationLogConstants.MAX_ERROR_LENGTH));
        e.setTargetTable(trimToNull(targetTable));
        e.setTargetId(trimToNull(targetId));
        e.setAfterValue(truncate(afterValue, OperationLogConstants.MAX_VALUE_LENGTH));
        // before_value 见 LogAspect 的类注释: 通用切面拿不到"改之前"的快照
        operationLogMapper.insert(e);
    }

    /**
     * 写日志, 但**不因为写日志失败而影响业务**。
     * <p>
     * 极端情况下(库连接断了、表被锁)写日志本身会抛异常。这时如果让它冒泡出去,
     * 一次**已经成功**的业务操作会变成"报错", 用户重试一遍就重复执行了 ——
     * 这是审计日志绝对不该造成的后果。所以这里只记错误日志, 不往外抛。
     */
    public void writeSafely(Long userId, OpType type, String targetTable, String targetId,
                            String afterValue, boolean success, String errorMessage) {
        try {
            write(userId, type, targetTable, targetId, afterValue, success, errorMessage);
        } catch (Exception e) {
            // 这里只打日志, 不再往外抛
            log.error("写操作日志失败(不影响业务): type={}, table={}, targetId={}",
                    type, targetTable, targetId, e);
        }
    }

    /** 空字符串和全空格都归一成 null */
    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    /** 超长截断。null 原样返回 */
    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }
}
