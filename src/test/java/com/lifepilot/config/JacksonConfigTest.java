package com.lifepilot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(JacksonConfig.class)
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesOffsetDateTimeResponses() throws Exception {
        String json = objectMapper.writeValueAsString(OffsetDateTime.parse("2026-07-10T10:00:00+08:00"));

        assertThat(json).isEqualTo("\"2026-07-10T10:00:00+08:00\"");
    }

    @Test
    void deserializesOffsetDateTimeWithoutOffset() throws Exception {
        OffsetDateTime value = objectMapper.readValue("\"2026-09-15T23:59:00\"", OffsetDateTime.class);

        assertThat(value.toLocalDateTime().toString()).isEqualTo("2026-09-15T23:59");
    }
}
