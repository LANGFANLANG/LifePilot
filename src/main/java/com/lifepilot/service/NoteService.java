package com.lifepilot.service;

import com.lifepilot.domain.Note;
import com.lifepilot.repository.NoteRepository;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 协调笔记用例，并将领域实体转换为服务层 DTO。
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;

    /**
     * 创建笔记应用服务。
     *
     * @param noteRepository 笔记持久化访问入口
     */
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * 根据创建命令新增笔记。
     *
     * @param command 笔记创建数据
     * @return 已创建的笔记视图
     */
    @Transactional
    public NoteView create(CreateNoteCommand command) {
        Note note = Note.create(command.title(), command.content());
        return NoteView.from(noteRepository.save(note));
    }

    /**
     * 按更新时间倒序获取笔记列表。
     *
     * @return 已排序的笔记视图列表
     */
    @Transactional(readOnly = true)
    public List<NoteView> list() {
        return noteRepository.findAll().stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(NoteView::from)
                .toList();
    }

    /**
     * 根据标识获取笔记。
     *
     * @param id 笔记标识
     * @return 笔记视图
     * @throws IllegalArgumentException 笔记不存在时抛出
     */
    @Transactional(readOnly = true)
    public NoteView get(UUID id) {
        return noteRepository.findById(id)
                .map(NoteView::from)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
    }
}
