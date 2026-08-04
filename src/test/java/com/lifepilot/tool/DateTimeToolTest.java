package com.lifepilot.tool;

import com.lifepilot.service.ExecutionLogService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DateTimeToolTest {

    @Test
    void returnsCurrentDateTimeUsingConfiguredClock() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-07-11T12:36:00Z"),
                ZoneId.of("Asia/Shanghai")
        );
        ExecutionLogService executionLogService = mock(ExecutionLogService.class);
        DateTimeTool tool = new DateTimeTool(clock, executionLogService);

        ToolResult result = tool.getCurrentDateTime();

        DateTimeTool.CurrentDateTime expected = new DateTimeTool.CurrentDateTime(
                "2026-07-11",
                "20:36:00",
                "星期六",
                "Asia/Shanghai",
                "2026-07-11T20:36:00+08:00"
        );
        assertThat(result).isEqualTo(ToolResult.success("current date and time retrieved", expected));
        verify(executionLogService).recordSuccess(null, "datetime.current", "", expected.toString());
    }
}
