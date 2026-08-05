package com.lifepilot.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 宽松的日期时间解析工具。
 *
 * <p>依次尝试带偏移的 ISO-8601、无偏移的本地时间、空格分隔的本地时间以及纯日期，
 * 缺失偏移时按系统默认时区补齐。</p>
 */
public final class TimeParser {

    private static final DateTimeFormatter SPACE_SEPARATED =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");

    private TimeParser() {
    }

    /**
     * 将字符串解析为带偏移的日期时间。
     *
     * @param text 日期时间字符串；可为 {@code null} 或空白
     * @return 解析结果；输入为空时返回 {@code null}
     * @throws DateTimeParseException 无法解析时抛出
     */
    public static OffsetDateTime parseOffsetDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.trim();
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他格式
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他格式
        }
        try {
            return LocalDateTime.parse(value, SPACE_SEPARATED)
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他格式
        }
        return LocalDate.parse(value)
                .atStartOfDay(ZoneId.systemDefault())
                .toOffsetDateTime();
    }
}
