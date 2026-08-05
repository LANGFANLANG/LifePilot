package com.lifepilot.api;

import cn.dev33.satoken.exception.NotLoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将应用异常转换为统一 API 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 将客户端非法参数转换为错误请求响应。
     *
     * @param ex 参数校验或资源查询异常
     * @return 统一的错误请求响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Result.failure(ErrorCode.BAD_REQUEST.name(), ex.getMessage()));
    }

    /**
     * 将请求体参数校验失败转换为错误请求响应。
     *
     * @param ex 请求体参数校验异常
     * @return 统一的错误请求响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "请求参数不合法" : error.getDefaultMessage())
                .orElse("请求参数不合法");
        return ResponseEntity.badRequest()
                .body(Result.failure(ErrorCode.BAD_REQUEST.name(), message));
    }

    /**
     * 将未登录异常转换为未授权响应。
     *
     * @param ex 未登录异常
     * @return 统一的未授权响应
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLogin(NotLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.failure(ErrorCode.UNAUTHORIZED.name(), "请先登录"));
    }

    /**
     * 记录未预期异常并返回通用服务端错误响应。
     *
     * @param ex 未预期异常
     * @return 统一的内部错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(ErrorCode.INTERNAL_ERROR.name(), "internal error"));
    }
}
