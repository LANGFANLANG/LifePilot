package com.lifepilot.service.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 今日执行计划的聚合展示数据。
 */
public record TodayPlanView(
        OffsetDateTime generatedAt,
        List<TodayPlanItemView> focus,
        List<TodayPlanItemView> timeline,
        List<TodayPlanItemView> inbox,
        int overdueCount,
        int pendingCount,
        int estimatedMinutes
) {
}
