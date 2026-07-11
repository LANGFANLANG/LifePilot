package com.lifepilot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP API 的最小安全基线配置。
 */
@Configuration
public class SecurityConfig {

    private final boolean authEnabled;

    /**
     * 创建安全配置。
     *
     * @param authEnabled 是否启用 API 认证
     */
    public SecurityConfig(@Value("${lifepilot.security.auth-enabled:false}") boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    /**
     * 创建无状态的 HTTP 安全过滤链。
     *
     * @param http Spring Security HTTP 配置对象
     * @return 安全过滤链
     * @throws Exception 配置过滤链失败时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/actuator/health").permitAll();
                    if (authEnabled) {
                        authorize.requestMatchers("/api/**").authenticated();
                    } else {
                        authorize.requestMatchers("/api/**").permitAll();
                    }
                    authorize.anyRequest().permitAll();
                });

        if (authEnabled) {
            http.httpBasic(httpBasic -> {
            });
        } else {
            http.httpBasic(AbstractHttpConfigurer::disable);
        }

        return http.build();
    }
}
