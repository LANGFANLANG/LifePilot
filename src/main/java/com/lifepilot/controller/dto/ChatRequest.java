package com.lifepilot.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * 发起 Agent 对话的 HTTP 请求参数。
 *
 * @param conversationId 可选的既有会话标识；为空时创建新会话
 * @param message 不可为空的用户消息
 */
public record ChatRequest(UUID conversationId, @NotBlank String message) {
}
