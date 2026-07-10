package com.lifepilot.service.dto;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 待办事项对外展示数据。
 *
 * @param id 待办事项标识
 * @param title 待办标题
 * @param description 可选的待办详情
 * @param status 当前待办状态
 * @param dueAt 可选的截止时间
 * @param createdAt 创建时间
 * @param updatedAt 最近修改时间
 */
public record TodoView(
        UUID id,
        String title,
        String description,
        TodoStatus status,
        OffsetDateTime dueAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * 将待办领域实体转换为展示数据。
     *
     * @param todo 待办领域实体
     * @return 待办事项展示数据
     */
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
