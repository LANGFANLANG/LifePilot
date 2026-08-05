package com.lifepilot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LenientOffsetDateTimeDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new SimpleModule().addDeserializer(OffsetDateTime.class, new LenientOffsetDateTimeDeserializer()));

    @Test
    void parsesOffsetDateTimeWithOffset() throws Exception {
        OffsetDateTime value = objectMapper.readValue("\"2026-09-15T23:59:00+08:00\"", OffsetDateTime.class);

        assertThat(value).isEqualTo(OffsetDateTime.parse("2026-09-15T23:59:00+08:00"));
    }

    @Test
    void parsesOffsetDateTimeWithoutOffsetUsingSystemZone() throws Exception {
        OffsetDateTime value = objectMapper.readValue("\"2026-09-15T23:59:00\"", OffsetDateTime.class);

        assertThat(value.toLocalDateTime().toString()).isEqualTo("2026-09-15T23:59");
        assertThat(value.getOffset()).isEqualTo(OffsetDateTime.now().getOffset());
    }

    @Test
    void parsesNullAsNull() throws Exception {
        OffsetDateTime value = objectMapper.readValue("null", OffsetDateTime.class);

        assertThat(value).isNull();
    }
}
