package com.lifepilot.memory.dto;

import com.lifepilot.domain.Conversation;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 会话对外展示数据。
 *
 * @param id 会话标识
 * @param title 会话标题
 * @param createdAt 创建时间
 * @param updatedAt 最近修改时间
 */
public record ConversationView(UUID id, String title, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

    /**
     * 将会话领域实体转换为展示数据。
     *
     * @param conversation 会话领域实体
     * @return 会话展示数据
     */
    public static ConversationView from(Conversation conversation) {
        return new ConversationView(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
