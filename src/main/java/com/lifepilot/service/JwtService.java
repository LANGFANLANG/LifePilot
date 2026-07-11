package com.lifepilot.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 创建和校验 JWT 访问令牌的应用服务。
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration tokenTtl;

    /**
     * 使用配置项创建 JWT 服务。
     *
     * @param secret JWT 签名密钥
     * @param tokenTtlSeconds 令牌有效期秒数
     */
    public JwtService(
            @Value("${lifepilot.security.jwt-secret}") String secret,
            @Value("${lifepilot.security.jwt-expiry-seconds:900}") long tokenTtlSeconds
    ) {
        this(secret, Duration.ofSeconds(tokenTtlSeconds));
    }

    /**
     * 使用指定密钥和有效期创建 JWT 服务。
     *
     * @param secret JWT 签名密钥
     * @param tokenTtl 令牌有效期
     */
    public JwtService(String secret, Duration tokenTtl) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenTtl = tokenTtl;
    }

    /**
     * 为指定主体创建访问令牌。
     *
     * @param subject 令牌主体
     * @return JWT 字符串
     */
    public String createToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenTtl)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析令牌主体。
     *
     * @param token JWT 字符串
     * @return 令牌主体
     * @throws IllegalArgumentException 令牌无效时抛出
     */
    public String parseSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid jwt token", ex);
        }
    }
}
