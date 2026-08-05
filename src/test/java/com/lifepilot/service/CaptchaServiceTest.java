package com.lifepilot.service;

import com.lifepilot.service.dto.CaptchaView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService(redis, 300);
    }

    @Test
    void createsCaptchaAndStoresAnswerWithTtl() {
        when(redis.opsForValue()).thenReturn(valueOperations);

        CaptchaView view = captchaService.create();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> answerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(keyCaptor.capture(), answerCaptor.capture(), ttlCaptor.capture());

        assertThat(view.captchaId()).isNotBlank();
        assertThat(view.imageBase64()).contains("base64");
        assertThat(keyCaptor.getValue()).startsWith("auth:captcha:");
        assertThat(answerCaptor.getValue()).matches("\\d+");
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofSeconds(300));
    }

    @Test
    void createdCaptchaCanBeConsumedWithStoredAnswer() {
        when(redis.opsForValue()).thenReturn(valueOperations);

        CaptchaView view = captchaService.create();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> answerCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), answerCaptor.capture(), any(Duration.class));
        when(valueOperations.getAndDelete(keyCaptor.getValue())).thenReturn(answerCaptor.getValue());

        captchaService.verifyAndConsume(view.captchaId(), answerCaptor.getValue());
    }

    @Test
    void rejectsWrongAnswer() {
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(anyString())).thenReturn("12");

        assertThatThrownBy(() -> captchaService.verifyAndConsume("captcha-1", "13"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("验证码错误");
    }

    @Test
    void rejectsExpiredCaptcha() {
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(anyString())).thenReturn(null);

        assertThatThrownBy(() -> captchaService.verifyAndConsume("captcha-1", "12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("验证码已失效，请刷新后重试");
    }

    @Test
    void rejectsBlankCode() {
        assertThatThrownBy(() -> captchaService.verifyAndConsume("captcha-1", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("请输入验证码");
    }
}
