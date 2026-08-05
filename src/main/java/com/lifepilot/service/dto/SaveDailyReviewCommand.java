package com.lifepilot.service.dto;

/**
 * 保存每日复盘的服务层命令。
 */
public record SaveDailyReviewCommand(
        String completedSummary,
        String unfinishedSummary,
        String newTasksSummary,
        String reflection,
        String tomorrowPlan
) {
}
