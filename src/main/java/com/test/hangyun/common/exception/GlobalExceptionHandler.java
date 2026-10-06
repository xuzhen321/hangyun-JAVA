package com.test.hangyun.common.exception;

import com.test.hangyun.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理: 把异常统一转换成 Result, 并用 HTTP 状态码区分错误类别。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常: 用它自带的状态码。
     * <p>
     * 注意 detail(比如具体的 id)只写进日志, 响应体里只放 msg ——
     * 内部 id 是排查用的实现细节, 不该暴露给调用方。
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        log.warn("业务异常: {}{}", e.getMessage(),
                e.getDetail() == null ? "" : " [" + e.getDetail() + "]");
        return ResponseEntity.status(e.getStatus()).body(Result.error(e.getMessage()));
    }

    /** 参数校验失败 -> 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError first = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String msg = first == null ? "参数校验失败" : first.getField() + " " + first.getDefaultMessage();
        log.warn("参数校验失败: {}", msg);
        return ResponseEntity.badRequest().body(Result.error(msg));
    }

    /**
     * 请求体读不出来 -> 400。
     * <p>
     * 典型场景: JSON 语法错误、字段类型对不上(比如时间传了 {@code 2026-10-05T15:17:14.831Z},
     * 带了毫秒和 Z, 不符合约定的两种格式)。
     * <p>
     * 必须单独接住: 它属于客户端传错了参数, 该返回 400 并让前端看见原因;
     * 若放任它落到下面的 Exception 兜底, 会变成 500"系统繁忙", 前端完全看不出是自己格式写错了。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Result.error("请求体格式不正确, 请检查字段类型和时间格式"));
    }

    /** 其他未捕获异常 -> 500, 堆栈只进日志, 不返回给前端 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("服务器内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙, 请稍后重试"));
    }
}
