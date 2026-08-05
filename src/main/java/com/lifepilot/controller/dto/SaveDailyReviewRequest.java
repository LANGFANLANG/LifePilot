package com.lifepilot.controller.dto;

/**
 * 保存每日复盘的 HTTP 请求参数。
 */
public record SaveDailyReviewRequest(
        String completedSummary,
        String unfinishedSummary,
        String newTasksSummary,
        String reflection,
        String tomorrowPlan
) {
}
