package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 持久化的自由文本笔记。
 */
@TableName("notes")
public class Note {

    @TableId
    private UUID id;

    private String title;

    private String content;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected Note() {
    }

    private Note(UUID id, String title, String content, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建带有自动生成标识和时间戳的笔记。
     *
     * @param title 笔记标题
     * @param content 笔记内容
     * @return 新建笔记
     */
    public static Note create(String title, String content) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Note(UUID.randomUUID(), title, content, now, now);
    }

    /**
     * 获取笔记标识。
     *
     * @return 笔记标识
     */
    public UUID getId() {
        return id;
    }

    /**
     * 获取笔记标题。
     *
     * @return 笔记标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取笔记内容。
     *
     * @return 笔记内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取最近修改时间。
     *
     * @return 最近修改时间
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
