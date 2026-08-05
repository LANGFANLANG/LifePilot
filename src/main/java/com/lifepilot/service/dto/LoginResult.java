package com.lifepilot.service.dto;

/**
 * 登录结果。
 *
 * @param token Sa-Token 访问令牌
 * @param user 当前登录用户
 */
public record LoginResult(String token, UserProfile user) {
}
