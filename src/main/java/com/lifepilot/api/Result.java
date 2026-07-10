package com.lifepilot.api;

public record Result<T>(boolean success, String code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "OK", "success", data);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null);
    }
}
