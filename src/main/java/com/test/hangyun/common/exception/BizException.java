package com.test.hangyun.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常。携带一个 HTTP 状态码, 因为 Result.code 只有 0/1,
 * 具体错误类别靠 HTTP 状态码区分。
 */
@Getter
public class BizException extends RuntimeException {

    private final HttpStatus status;

    public BizException(String msg) {
        this(HttpStatus.BAD_REQUEST, msg);
    }

    public BizException(HttpStatus status, String msg) {
        super(msg);
        this.status = status;
    }

    /** 资源不存在 -> 404 */
    public static BizException notFound(String msg) {
        return new BizException(HttpStatus.NOT_FOUND, msg);
    }

    /** 数据冲突(关联被引用、唯一键重复) -> 409 */
    public static BizException conflict(String msg) {
        return new BizException(HttpStatus.CONFLICT, msg);
    }
}
