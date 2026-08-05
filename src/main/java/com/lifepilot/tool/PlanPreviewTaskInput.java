package com.lifepilot.tool;

import com.lifepilot.domain.TodoPriority;

/**
 * AI 工具创建计划草案时传入的单个任务。
 */
public record PlanPreviewTaskInput(
        String title,
        String description,
        String dueAt,
        TodoPriority priority,
        String category,
        Integer estimatedMinutes,
        String plannedStartAt,
        String reminderAt
) {
}
