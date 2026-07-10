package com.lifepilot.service.dto;

import java.time.OffsetDateTime;

public record CreateTodoCommand(String title, String description, OffsetDateTime dueAt) {
}
