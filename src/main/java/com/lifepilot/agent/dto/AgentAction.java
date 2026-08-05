package com.lifepilot.agent.dto;

import java.util.UUID;

/**
 * Agent 回复中可供前端执行或展示的结构化动作。
 */
public record AgentAction(
        String type,
        UUID resourceId,
        String label
) {
}
