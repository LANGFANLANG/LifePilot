package com.lifepilot.service.dto;

import java.time.OffsetDateTime;

/**
 * 服务层创建待办事项命令。
 *
 * @param title 待办标题
 * @param description 可选的待办详情
 * @param dueAt 可选的截止时间
 */
public record CreateTodoCommand(String title, String description, OffsetDateTime dueAt) {
}
