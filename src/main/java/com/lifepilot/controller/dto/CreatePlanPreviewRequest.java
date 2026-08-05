package com.lifepilot.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * 创建 AI 计划草案的 HTTP 请求参数。
 */
public record CreatePlanPreviewRequest(
        UUID conversationId,
        @NotBlank String goal,
        @NotEmpty List<@Valid PlanPreviewTaskRequest> tasks
) {
}
