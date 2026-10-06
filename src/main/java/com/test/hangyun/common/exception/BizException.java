package com.test.hangyun.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常。携带一个 HTTP 状态码, 因为 Result.code 只有 0/1,
 * 具体错误类别靠 HTTP 状态码区分。
 * <p>
 * msg 是给**前端**看的文案, 里面不要出现客户 ID、订单号这类内部实现细节;
 * 排查需要的具体值走 {@link #detail}, 它只会进服务端日志, 不会出现在响应体里。
 */
@Getter
public class BizException extends RuntimeException {

    private final HttpStatus status;

    /**
     * 只进日志、不返回前端的补充信息, 比如具体的 id。
     * 由 GlobalExceptionHandler 记录, 拼进响应体是明确禁止的。
     */
    private final String detail;

    public BizException(String msg) {
        this(HttpStatus.BAD_REQUEST, msg, null);
    }

    /** detail 只出现在服务端日志里, 响应体里仍然只有 msg */
    public BizException(String msg, String detail) {
        this(HttpStatus.BAD_REQUEST, msg, detail);
    }

    public BizException(HttpStatus status, String msg) {
        this(status, msg, null);
    }

    public BizException(HttpStatus status, String msg, String detail) {
        super(msg);
        this.status = status;
        this.detail = detail;
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
