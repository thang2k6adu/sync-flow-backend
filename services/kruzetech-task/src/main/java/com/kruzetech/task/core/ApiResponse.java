package com.kruzetech.task.core;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Envelope chung, cùng dạng với ApiResponse của nest-boilerplate. */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(boolean error, int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return new ApiResponse<>(false, 0, "Success", data, traceId);
    }

    public static ApiResponse<Object> fail(int code, String message, String traceId) {
        return new ApiResponse<>(true, code, message, null, traceId);
    }
}
