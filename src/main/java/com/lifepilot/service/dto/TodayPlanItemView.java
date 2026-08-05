package com.lifepilot.service.dto;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 今日计划中的单个待办展示项。
 */
public record TodayPlanItemView(
        UUID id,
        String title,
        String description,
        TodoStatus status,
        OffsetDateTime dueAt,
        TodoPriority priority,
        String category,
        Integer estimatedMinutes,
        OffsetDateTime plannedStartAt,
        OffsetDateTime reminderAt,
        boolean overdue,
        int rank
) {

    public static TodayPlanItemView from(Todo todo, boolean overdue, int rank) {
        return new TodayPlanItemView(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                todo.getDueAt(),
                todo.getPriority(),
                todo.getCategory(),
                todo.getEstimatedMinutes(),
                todo.getPlannedStartAt(),
                todo.getReminderAt(),
                overdue,
                rank
        );
    }
}
