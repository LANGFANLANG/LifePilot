package com.lifepilot.memory.dto;

import com.lifepilot.domain.ChatMessage;
import com.lifepilot.domain.ChatRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 聊天消息对外展示数据。
 *
 * @param id 消息标识
 * @param conversationId 所属会话标识
 * @param role 消息发送方角色
 * @param content 消息内容
 * @param createdAt 创建时间
 */
public record MessageView(UUID id, UUID conversationId, ChatRole role, String content, OffsetDateTime createdAt) {

    /**
     * 将聊天消息领域实体转换为展示数据。
     *
     * @param message 聊天消息领域实体
     * @return 消息展示数据
     */
    public static MessageView from(ChatMessage message) {
        return new MessageView(
                message.getId(),
                message.getConversationId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
