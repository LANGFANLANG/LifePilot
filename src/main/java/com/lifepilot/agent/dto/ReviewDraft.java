package com.lifepilot.agent.dto;

/**
 * AI 或确定性逻辑生成的复盘草稿内容。
 */
public record ReviewDraft(
        String completedSummary,
        String unfinishedSummary,
        String newTasksSummary,
        String reflection,
        String tomorrowPlan
) {
}
