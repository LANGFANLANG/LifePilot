package com.lifepilot.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test-secret-test-secret-test-secret-test-secret",
            Duration.ofMinutes(5)
    );

    @Test
    void createsTokenWithSubject() {
        String token = jwtService.createToken("local-user");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void parsesSubjectFromToken() {
        String token = jwtService.createToken("local-user");

        String subject = jwtService.parseSubject(token);

        assertThat(subject).isEqualTo("local-user");
    }

    @Test
    void rejectsInvalidToken() {
        assertThatThrownBy(() -> jwtService.parseSubject("not-a-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid jwt token");
    }
}
