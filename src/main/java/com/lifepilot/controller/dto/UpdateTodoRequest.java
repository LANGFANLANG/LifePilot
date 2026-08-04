package com.lifepilot.controller.dto;

import com.lifepilot.domain.TodoPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateTodoRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        OffsetDateTime dueAt,
        TodoPriority priority,
        @Size(max = 80) String category,
        @PositiveOrZero Integer estimatedMinutes,
        OffsetDateTime plannedStartAt,
        OffsetDateTime reminderAt,
        UUID parentTodoId,
        @Size(max = 40) String source
) {
}
