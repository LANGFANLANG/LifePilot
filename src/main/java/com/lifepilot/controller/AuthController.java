package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.LoginRequest;
import com.lifepilot.controller.dto.RegisterRequest;
import com.lifepilot.service.AuthService;
import com.lifepilot.service.CaptchaService;
import com.lifepilot.service.dto.CaptchaView;
import com.lifepilot.service.dto.LoginResult;
import com.lifepilot.service.dto.UserProfile;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供验证码获取、注册、登录和会话管理接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * 创建认证接口控制器。
     *
     * @param authService 登录认证应用服务
     * @param captchaService 验证码应用服务
     */
    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    /**
     * 获取算术验证码图片。
     *
     * @return 验证码标识与图片数据
     */
    @GetMapping("/captcha")
    public Result<CaptchaView> captcha() {
        return Result.success(captchaService.create());
    }

    /**
     * 注册新用户。
     *
     * @param request 已校验的注册请求
     * @return 已注册用户的公开资料
     */
    @PostMapping("/register")
    public Result<UserProfile> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(
                request.username(),
                request.password(),
                request.displayName(),
                request.captchaId(),
                request.captchaCode()
        ));
    }

    /**
     * 账号密码登录。
     *
     * @param request 已校验的登录请求
     * @return 访问令牌与用户公开资料
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(
                request.username(),
                request.password(),
                request.captchaId(),
                request.captchaCode()
        ));
    }

    /**
     * 退出当前登录会话。
     *
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success(null);
    }

    /**
     * 查询当前登录用户。
     *
     * @return 当前登录用户的公开资料
     */
    @GetMapping("/me")
    public Result<UserProfile> me() {
        return Result.success(authService.currentUser());
    }
}
