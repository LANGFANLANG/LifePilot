package com.lifepilot.agent.dto;

import java.util.UUID;

/**
 * Agent 对话请求数据。
 *
 * @param conversationId 可选的既有会话标识；为空时创建新会话
 * @param message 用户输入消息
 */
public record AgentRequest(UUID conversationId, String message) {
}
