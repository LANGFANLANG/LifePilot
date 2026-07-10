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

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Transactional
    public NoteView create(CreateNoteCommand command) {
        Note note = Note.create(command.title(), command.content());
        return NoteView.from(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<NoteView> list() {
        return noteRepository.findAll().stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(NoteView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteView get(UUID id) {
        return noteRepository.findById(id)
                .map(NoteView::from)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
    }
}
