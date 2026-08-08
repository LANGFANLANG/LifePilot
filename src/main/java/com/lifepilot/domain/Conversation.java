package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 持久化的聊天会话。
 */
@TableName("conversations")
public class Conversation {

    @TableId
    private UUID id;

    private String title;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected Conversation() {
    }

    private Conversation(UUID id, String title, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建聊天会话。
     *
     * @param title 会话标题
     * @return 新建会话
     */
    public static Conversation create(String title) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Conversation(UUID.randomUUID(), title, now, now);
    }

    /**
     * 为当前会话创建一条消息并更新会话修改时间。
     *
     * @param role 消息发送方角色
     * @param content 消息内容
     * @return 新建消息
     */
    public ChatMessage addMessage(ChatRole role, String content) {
        updatedAt = OffsetDateTime.now();
        return ChatMessage.create(this, role, content);
    }

    /**
     * 获取会话标识。
     *
     * @return 会话标识
     */
    public UUID getId() {
        return id;
    }

    /**
     * 获取会话标题。
     *
     * @return 会话标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取会话创建时间。
     *
     * @return 创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取会话最近修改时间。
     *
     * @return 最近修改时间
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
