package com.lifepilot.api;

/**
 * API 对外暴露的应用级错误码。
 */
public enum ErrorCode {
    /** 请求数据不合法。 */
    BAD_REQUEST,
    /** 未认证或认证信息无效。 */
    UNAUTHORIZED,
    /** 请求的资源不存在。 */
    NOT_FOUND,
    /** 发生未预期的服务端错误。 */
    INTERNAL_ERROR
}
