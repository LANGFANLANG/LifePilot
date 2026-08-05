package com.lifepilot.service.dto;

/**
 * 验证码对外展示数据。
 *
 * @param captchaId 验证码标识
 * @param imageBase64 验证码图片的 Base64 编码
 */
public record CaptchaView(String captchaId, String imageBase64) {
}
