package com.lifepilot.service.dto;

import com.lifepilot.domain.Note;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NoteView(
        UUID id,
        String title,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static NoteView from(Note note) {
        return new NoteView(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
