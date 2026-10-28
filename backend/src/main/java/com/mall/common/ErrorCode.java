package com.mall.common;

import lombok.Getter;

/**
 * HTTP 状态与业务错误码分开保存，前端可以同时根据状态码和 code 处理。
 */
@Getter
public enum ErrorCode {
    BAD_REQUEST(400, 40000),
    VALIDATION(400, 40001),
    UNAUTHORIZED(401, 40100),
    FORBIDDEN(403, 40300),
    NOT_FOUND(404, 40400),
    CONFLICT(409, 40900),
    BUSINESS(422, 42200);

    private final int httpStatus;
    private final int code;

    ErrorCode(int httpStatus, int code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }
}
