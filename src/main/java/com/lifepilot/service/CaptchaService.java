package com.lifepilot.service;

import com.lifepilot.service.dto.CaptchaView;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 生成并校验算术验证码的应用服务。
 *
 * <p>验证码答案按一次性语义保存在 Redis 中，取用即失效。</p>
 */
@Service
public class CaptchaService {

    private static final String KEY_PREFIX = "auth:captcha:";

    private final StringRedisTemplate redis;
    private final Duration expire;

    /**
     * 创建验证码服务。
     *
     * @param redis Redis 字符串操作客户端
     * @param expireSeconds 验证码有效期秒数
     */
    public CaptchaService(
            StringRedisTemplate redis,
            @Value("${lifepilot.captcha.expire-seconds:300}") long expireSeconds
    ) {
        this.redis = redis;
        this.expire = Duration.ofSeconds(expireSeconds);
    }

    /**
     * 生成一张算术验证码图片并缓存答案。
     *
     * @return 验证码标识与图片数据
     */
    public CaptchaView create() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        String captchaId = UUID.randomUUID().toString();
        redis.opsForValue().set(KEY_PREFIX + captchaId, captcha.text(), expire);
        return new CaptchaView(captchaId, captcha.toBase64());
    }

    /**
     * 校验验证码并使其一次性失效。
     *
     * @param captchaId 验证码标识
     * @param code 用户提交的答案
     * @throws IllegalArgumentException 验证码缺失、已失效或答案错误时抛出
     */
    public void verifyAndConsume(String captchaId, String code) {
        if (captchaId == null || captchaId.isBlank() || code == null || code.isBlank()) {
            throw new IllegalArgumentException("请输入验证码");
        }
        String expected = redis.opsForValue().getAndDelete(KEY_PREFIX + captchaId);
        if (expected == null) {
            throw new IllegalArgumentException("验证码已失效，请刷新后重试");
        }
        if (!expected.equals(code.trim())) {
            throw new IllegalArgumentException("验证码错误");
        }
    }
}
