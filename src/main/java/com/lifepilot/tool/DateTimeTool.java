package com.lifepilot.tool;

import com.lifepilot.service.ExecutionLogService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 向 AI 助手提供当前日期和时间。
 */
@Component
public class DateTimeTool {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter WEEKDAY_FORMAT = DateTimeFormatter.ofPattern("EEEE", Locale.SIMPLIFIED_CHINESE);

    private final Clock clock;
    private final ExecutionLogService executionLogService;

    /**
     * 创建日期时间工具。
     *
     * @param clock 用于读取当前时间的时钟
     * @param executionLogService 工具执行日志服务
     */
    public DateTimeTool(Clock clock, ExecutionLogService executionLogService) {
        this.clock = clock;
        this.executionLogService = executionLogService;
    }

    /**
     * 获取应用所在时区的当前日期和时间。
     *
     * @return 包含日期、时间、星期、时区和 ISO 时间的工具结果
     */
    @Tool(description = "获取当前日期、时间、星期和系统时区。回答今天几号、星期几、现在几点等问题时使用")
    public ToolResult getCurrentDateTime() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        CurrentDateTime value = new CurrentDateTime(
                now.format(DATE_FORMAT),
                now.format(TIME_FORMAT),
                now.format(WEEKDAY_FORMAT),
                clock.getZone().getId(),
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
        executionLogService.recordSuccess(null, "datetime.current", "", value.toString());
        return ToolResult.success("current date and time retrieved", value);
    }

    /**
     * 日期时间工具的结构化返回值。
     */
    public record CurrentDateTime(String date, String time, String dayOfWeek, String zoneId, String isoDateTime) {
    }
}
