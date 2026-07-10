package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.CreateNoteRequest;
import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public Result<NoteView> create(@Valid @RequestBody CreateNoteRequest request) {
        return Result.success(noteService.create(new CreateNoteCommand(request.title(), request.content())));
    }

    @GetMapping
    public Result<List<NoteView>> list() {
        return Result.success(noteService.list());
    }

    @GetMapping("/{id}")
    public Result<NoteView> get(@PathVariable UUID id) {
        return Result.success(noteService.get(id));
    }
}
