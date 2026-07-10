package com.lifepilot.memory;

import com.lifepilot.domain.ChatMessage;
import com.lifepilot.domain.ChatRole;
import com.lifepilot.domain.Conversation;
import com.lifepilot.memory.dto.ConversationView;
import com.lifepilot.memory.dto.MessageView;
import com.lifepilot.repository.ChatMessageRepository;
import com.lifepilot.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 管理聊天会话和消息的持久化记忆。
 */
@Service
public class ChatMemoryService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 创建聊天记忆服务。
     *
     * @param conversationRepository 会话持久化访问入口
     * @param chatMessageRepository 消息持久化访问入口
     */
    public ChatMemoryService(
            ConversationRepository conversationRepository,
            ChatMessageRepository chatMessageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 创建新的聊天会话。
     *
     * @param title 会话标题
     * @return 已创建的会话展示数据
     */
    @Transactional
    public ConversationView createConversation(String title) {
        return ConversationView.from(conversationRepository.save(Conversation.create(title)));
    }

    /**
     * 将消息追加到指定会话。
     *
     * @param conversationId 会话标识
     * @param role 消息发送方角色
     * @param content 消息内容
     * @return 已保存的消息展示数据
     * @throws IllegalArgumentException 会话不存在时抛出
     */
    @Transactional
    public MessageView appendMessage(UUID conversationId, ChatRole role, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ChatMessage message = conversation.addMessage(role, content);
        conversationRepository.save(conversation);
        return MessageView.from(chatMessageRepository.save(message));
    }

    /**
     * 按创建时间正序读取指定会话的消息。
     *
     * @param conversationId 会话标识
     * @return 会话消息展示数据列表
     */
    @Transactional(readOnly = true)
    public List<MessageView> loadRecentMessages(UUID conversationId) {
        return chatMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageView::from)
                .toList();
    }
}
