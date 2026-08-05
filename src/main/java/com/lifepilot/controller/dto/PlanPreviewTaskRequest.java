package com.lifepilot.controller.dto;

import com.lifepilot.domain.TodoPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * 创建 AI 计划草案任务的 HTTP 请求参数。
 */
public record PlanPreviewTaskRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        OffsetDateTime dueAt,
        TodoPriority priority,
        @Size(max = 80) String category,
        @PositiveOrZero Integer estimatedMinutes,
        OffsetDateTime plannedStartAt,
        OffsetDateTime reminderAt
) {
}
