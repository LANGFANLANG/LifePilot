package com.lifepilot.service.dto;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TodoView(
        UUID id,
        String title,
        String description,
        TodoStatus status,
        OffsetDateTime dueAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TodoView from(Todo todo) {
        return new TodoView(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                todo.getDueAt(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
