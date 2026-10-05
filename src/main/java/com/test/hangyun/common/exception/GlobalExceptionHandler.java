package com.test.hangyun.common.exception;

import com.test.hangyun.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /** 业务异常: 用它自带的状态码 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
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

    /** 其他未捕获异常 -> 500, 堆栈只进日志, 不返回给前端 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("服务器内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙, 请稍后重试"));
    }
}
