package com.lifepilot.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void successWrapsData() {
        Result<String> result = Result.success("ok");

        assertThat(result.success()).isTrue();
        assertThat(result.code()).isEqualTo("OK");
        assertThat(result.data()).isEqualTo("ok");
    }

    @Test
    void failureWrapsError() {
        Result<Void> result = Result.failure("BAD_REQUEST", "bad input");

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("BAD_REQUEST");
        assertThat(result.message()).isEqualTo("bad input");
    }
}
