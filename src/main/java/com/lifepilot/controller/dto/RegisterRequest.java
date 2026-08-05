package com.lifepilot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。
 *
 * @param username 登录账号
 * @param password 明文密码
 * @param displayName 可选的显示名称
 * @param captchaId 验证码标识
 * @param captchaCode 验证码答案
 */
public record RegisterRequest(
        @NotBlank(message = "请输入用户名")
        @Size(min = 3, max = 64, message = "用户名长度需在 3 到 64 个字符之间")
        String username,
        @NotBlank(message = "请输入密码")
        @Size(min = 6, max = 72, message = "密码长度需在 6 到 72 个字符之间")
        String password,
        @Size(max = 64, message = "显示名称长度不能超过 64 个字符")
        String displayName,
        @NotBlank(message = "请输入验证码")
        String captchaId,
        @NotBlank(message = "请输入验证码")
        String captchaCode
) {
}
