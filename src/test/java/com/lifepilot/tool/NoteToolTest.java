package com.lifepilot.tool;

import com.lifepilot.service.ExecutionLogService;
import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteToolTest {

    @Mock
    private NoteService noteService;

    @Mock
    private ExecutionLogService executionLogService;

    @InjectMocks
    private NoteTool noteTool;

    @Test
    void createsNoteThroughService() {
        NoteView note = note("Meeting notes", "Discuss the MVP scope");
        when(noteService.create(new CreateNoteCommand("Meeting notes", "Discuss the MVP scope")))
                .thenReturn(note);

        ToolResult result = noteTool.createNote("Meeting notes", "Discuss the MVP scope");

        ArgumentCaptor<CreateNoteCommand> captor = ArgumentCaptor.forClass(CreateNoteCommand.class);
        verify(noteService).create(captor.capture());
        verify(executionLogService).recordSuccess(null, "note.create", "Meeting notes", note.toString());
        assertThat(captor.getValue().title()).isEqualTo("Meeting notes");
        assertThat(result).isEqualTo(ToolResult.success("note created", note));
    }

    @Test
    void listsNotesThroughService() {
        List<NoteView> notes = List.of(note("Meeting notes", "Discuss the MVP scope"));
        when(noteService.list()).thenReturn(notes);

        ToolResult result = noteTool.listNotes();

        verify(noteService).list();
        verify(executionLogService).recordSuccess(null, "note.list", "", notes.toString());
        assertThat(result).isEqualTo(ToolResult.success("notes listed", notes));
    }

    @Test
    void getsNoteThroughService() {
        UUID noteId = UUID.randomUUID();
        NoteView note = note("Meeting notes", "Discuss the MVP scope");
        when(noteService.get(noteId)).thenReturn(note);

        ToolResult result = noteTool.getNote(noteId);

        verify(noteService).get(noteId);
        verify(executionLogService).recordSuccess(null, "note.get", noteId.toString(), note.toString());
        assertThat(result).isEqualTo(ToolResult.success("note found", note));
    }

    @Test
    void recordsFailedNoteExecutionAndRethrows() {
        UUID noteId = UUID.randomUUID();
        when(noteService.get(noteId)).thenThrow(new IllegalArgumentException("note not found"));

        assertThatThrownBy(() -> noteTool.getNote(noteId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("note not found");

        verify(executionLogService).recordFailure(null, "note.get", noteId.toString(), "note not found");
    }

    private static NoteView note(String title, String content) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new NoteView(UUID.randomUUID(), title, content, now, now);
    }
}
