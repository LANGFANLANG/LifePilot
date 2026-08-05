package com.lifepilot.service.dto;

import com.lifepilot.domain.PlanPreviewTask;
import com.lifepilot.domain.TodoPriority;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AI 计划草案任务的展示数据。
 */
public record PlanPreviewTaskView(
        UUID id,
        String title,
        String description,
        OffsetDateTime dueAt,
        TodoPriority priority,
        String category,
        Integer estimatedMinutes,
        OffsetDateTime plannedStartAt,
        OffsetDateTime reminderAt,
        int sortOrder,
        OffsetDateTime createdAt
) {

    public static PlanPreviewTaskView from(PlanPreviewTask task) {
        return new PlanPreviewTaskView(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueAt(),
                task.getPriority(),
                task.getCategory(),
                task.getEstimatedMinutes(),
                task.getPlannedStartAt(),
                task.getReminderAt(),
                task.getSortOrder(),
                task.getCreatedAt()
        );
    }
}
