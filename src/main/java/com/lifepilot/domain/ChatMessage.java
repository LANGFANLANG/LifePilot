package com.lifepilot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 持久化的会话消息。
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ChatRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected ChatMessage() {
    }

    private ChatMessage(UUID id, Conversation conversation, ChatRole role, String content, OffsetDateTime createdAt) {
        this.id = id;
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * 创建归属指定会话的消息。
     *
     * @param conversation 所属会话
     * @param role 消息发送方角色
     * @param content 消息内容
     * @return 新建消息
     */
    public static ChatMessage create(Conversation conversation, ChatRole role, String content) {
        return new ChatMessage(UUID.randomUUID(), conversation, role, content, OffsetDateTime.now());
    }

    /**
     * 获取消息标识。
     *
     * @return 消息标识
     */
    public UUID getId() {
        return id;
    }

    /**
     * 获取所属会话标识。
     *
     * @return 所属会话标识
     */
    public UUID getConversationId() {
        return conversation.getId();
    }

    /**
     * 获取消息发送方角色。
     *
     * @return 消息角色
     */
    public ChatRole getRole() {
        return role;
    }

    /**
     * 获取消息内容。
     *
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取消息创建时间。
     *
     * @return 创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
