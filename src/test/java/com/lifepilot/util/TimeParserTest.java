package com.lifepilot.util;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeParserTest {

    @Test
    void parsesIsoStringWithOffset() {
        OffsetDateTime value = TimeParser.parseOffsetDateTime("2026-09-15T23:59:00+08:00");

        assertThat(value).isEqualTo(OffsetDateTime.parse("2026-09-15T23:59:00+08:00"));
    }

    @Test
    void parsesIsoStringWithoutOffsetUsingSystemZone() {
        OffsetDateTime value = TimeParser.parseOffsetDateTime("2026-09-15T23:59:00");

        assertThat(value.toLocalDateTime().toString()).isEqualTo("2026-09-15T23:59");
        assertThat(value.getOffset()).isEqualTo(OffsetDateTime.now().getOffset());
    }

    @Test
    void parsesSpaceSeparatedLocalDateTime() {
        OffsetDateTime value = TimeParser.parseOffsetDateTime("2026-09-15 23:59");

        assertThat(value.toLocalDateTime().toString()).isEqualTo("2026-09-15T23:59");
    }

    @Test
    void parsesPlainDateAsStartOfDay() {
        OffsetDateTime value = TimeParser.parseOffsetDateTime("2026-09-15");

        assertThat(value.toLocalDateTime().toString()).isEqualTo("2026-09-15T00:00");
    }

    @Test
    void returnsNullForBlankInput() {
        assertThat(TimeParser.parseOffsetDateTime("  ")).isNull();
        assertThat(TimeParser.parseOffsetDateTime(null)).isNull();
    }
}
