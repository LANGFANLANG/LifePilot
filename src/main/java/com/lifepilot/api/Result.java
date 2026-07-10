package com.lifepilot.api;

/**
 * HTTP 接口统一响应包装。
 *
 * @param <T> 响应数据类型
 * @param success 请求是否成功
 * @param code 应用级响应码
 * @param message 面向调用方的响应消息
 * @param data 响应数据；失败时为 {@code null}
 */
public record Result<T>(boolean success, String code, String message, T data) {

    /**
     * 创建成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应包装
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, "OK", "success", data);
    }

    /**
     * 创建不含响应数据的失败响应。
     *
     * @param code 应用级错误码
     * @param message 面向调用方的错误消息
     * @param <T> 响应数据类型
     * @return 失败响应包装
     */
    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null);
    }
}
