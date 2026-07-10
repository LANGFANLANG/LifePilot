package com.lifepilot.repository;

import com.lifepilot.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * {@link ChatMessage} 实体的持久化访问入口。
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * 按创建时间正序查询指定会话的全部消息。
     *
     * @param conversationId 会话标识
     * @return 会话消息列表
     */
    List<ChatMessage> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);
}
