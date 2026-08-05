package com.lifepilot.agent.dto;

import java.util.List;
import java.util.UUID;

/**
 * Agent 对话响应数据。
 *
 * @param conversationId 本次对话所属会话标识
 * @param content 助手回复内容
 */
public record AgentResponse(UUID conversationId, String content, List<AgentAction> actions) {

    public AgentResponse(UUID conversationId, String content) {
        this(conversationId, content, List.of());
    }
}
