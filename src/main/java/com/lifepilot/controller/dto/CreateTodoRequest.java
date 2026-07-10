package com.lifepilot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * 创建待办事项的 HTTP 请求参数。
 *
 * @param title 待办标题，长度不超过 200 个字符
 * @param description 可选的待办详情
 * @param dueAt 可选的截止时间
 */
public record CreateTodoRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        OffsetDateTime dueAt
) {
}
