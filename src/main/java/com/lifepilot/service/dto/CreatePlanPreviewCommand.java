package com.lifepilot.service.dto;

import java.util.List;
import java.util.UUID;

/**
 * 创建 AI 计划草案的服务层命令。
 */
public record CreatePlanPreviewCommand(
        UUID conversationId,
        String goal,
        List<CreateTodoCommand> tasks
) {
}
