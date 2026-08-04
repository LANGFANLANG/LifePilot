package com.lifepilot.service.dto;

import com.lifepilot.domain.TodoPriority;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateTodoCommand(
        String title,
        String description,
        OffsetDateTime dueAt,
        TodoPriority priority,
        String category,
        Integer estimatedMinutes,
        OffsetDateTime plannedStartAt,
        OffsetDateTime reminderAt,
        UUID parentTodoId,
        String source
) {
}
