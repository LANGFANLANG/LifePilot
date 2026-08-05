package com.lifepilot.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 Sa-Token 路由拦截，实现 API 登录鉴权。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private final boolean authEnabled;

    /**
     * 创建 Sa-Token 拦截配置。
     *
     * @param authEnabled 是否启用 API 登录鉴权
     */
    public SaTokenConfig(@Value("${lifepilot.security.auth-enabled:false}") boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            if (authEnabled) {
                SaRouter.match("/api/**")
                        .notMatch("/api/auth/login", "/api/auth/register", "/api/auth/captcha")
                        .check(r -> StpUtil.checkLogin());
            }
        })).addPathPatterns("/**");
    }
}
