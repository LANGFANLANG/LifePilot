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

    private String sourceType;

    private String originalFilename;

    private String contentType;

    private String storedFilename;

    private Long fileSize;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected Note() {
    }

    private Note(
            UUID id,
            String title,
            String content,
            String sourceType,
            String originalFilename,
            String contentType,
            String storedFilename,
            Long fileSize,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.sourceType = sourceType;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.storedFilename = storedFilename;
        this.fileSize = fileSize;
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
        return new Note(UUID.randomUUID(), title, content, "TEXT", null, null, null, null, now, now);
    }

    /**
     * 创建由上传文件生成的笔记。
     *
     * @param title 笔记标题
     * @param content 可预览的文本内容
     * @param originalFilename 原始文件名
     * @param contentType 文件内容类型
     * @param storedFilename 本地存储文件名
     * @param fileSize 文件大小
     * @return 新建文件笔记
     */
    public static Note createFileNote(
            String title,
            String content,
            String originalFilename,
            String contentType,
            String storedFilename,
            long fileSize
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Note(UUID.randomUUID(), title, content, "FILE", originalFilename, contentType, storedFilename, fileSize, now, now);
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
     * 获取笔记来源类型。
     *
     * @return TEXT 或 FILE
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * 获取上传文件原名。
     *
     * @return 上传文件原名
     */
    public String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * 获取文件内容类型。
     *
     * @return 文件内容类型
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 获取本地存储文件名。
     *
     * @return 本地存储文件名
     */
    public String getStoredFilename() {
        return storedFilename;
    }

    /**
     * 获取文件大小。
     *
     * @return 文件大小
     */
    public Long getFileSize() {
        return fileSize;
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
