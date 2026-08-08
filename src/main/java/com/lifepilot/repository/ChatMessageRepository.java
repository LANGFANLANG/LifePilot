package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

/**
 * {@link ChatMessage} 实体的持久化访问入口。
 */
@Mapper
public interface ChatMessageRepository extends MyBatisRepository<ChatMessage> {

    /**
     * 按创建时间正序查询指定会话的全部消息。
     *
     * @param conversationId 会话标识
     * @return 会话消息列表
     */
    default List<ChatMessage> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId) {
        return selectList(Wrappers.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt));
    }

    default ChatMessage save(ChatMessage message) {
        return save(message, ChatMessage::getId);
    }
}
