package com.lifepilot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateTodoRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        OffsetDateTime dueAt
) {
}
