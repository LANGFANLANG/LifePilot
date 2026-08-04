package com.lifepilot.service.dto;

import com.lifepilot.domain.TodoPriority;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 服务层创建待办事项命令。
 *
 * @param title 待办标题
 * @param description 可选的待办详情
 * @param dueAt 可选的截止时间
 */
public record CreateTodoCommand(
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

    public CreateTodoCommand(String title, String description, OffsetDateTime dueAt) {
        this(title, description, dueAt, TodoPriority.MEDIUM, null, null, null, null, null, "manual");
    }
}
