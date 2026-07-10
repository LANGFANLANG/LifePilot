package com.lifepilot.repository;

import com.lifepilot.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * {@link Note} 实体的持久化访问入口。
 */
public interface NoteRepository extends JpaRepository<Note, UUID> {
}
