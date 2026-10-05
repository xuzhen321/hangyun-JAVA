package com.test.hangyun.common;

import lombok.Getter;
import lombok.ToString;

/**
 * 统一响应体。
 * code 只有两个取值: 1 表示成功, 0 表示失败。
 * 具体的错误类别由 HTTP 状态码承载, 详见 GlobalExceptionHandler。
 * <p>
 * msg 只在失败时有意义, 用来告诉用户出了什么问题; 成功时一律为 null, 前端不需要读它。
 */
@Getter
@ToString
public class Result<T> {

    /** 成功 */
    public static final int SUCCESS = 1;
    /** 失败 */
    public static final int FAIL = 0;

    private final Integer code;
    private final String msg;
    private final T data;

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS, null, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, null, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(FAIL, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
