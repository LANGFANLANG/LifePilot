package com.lifepilot.service.dto;

import com.lifepilot.domain.Note;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 笔记对外展示数据。
 *
 * @param id 笔记标识
 * @param title 笔记标题
 * @param content 笔记内容
 * @param createdAt 创建时间
 * @param updatedAt 最近修改时间
 */
public record NoteView(
        UUID id,
        String title,
        String content,
        String sourceType,
        String originalFilename,
        String contentType,
        Long fileSize,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * 将笔记领域实体转换为展示数据。
     *
     * @param note 笔记领域实体
     * @return 笔记展示数据
     */
    public static NoteView from(Note note) {
        return new NoteView(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getSourceType(),
                note.getOriginalFilename(),
                note.getContentType(),
                note.getFileSize(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
