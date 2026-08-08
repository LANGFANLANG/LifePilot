package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.Note;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link Note} 实体的持久化访问入口。
 */
@Mapper
public interface NoteRepository extends MyBatisRepository<Note> {

    default Note save(Note note) {
        return save(note, Note::getId);
    }

    default Optional<Note> findById(UUID id) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(Note.class)
                .eq(Note::getId, id)));
    }
}
