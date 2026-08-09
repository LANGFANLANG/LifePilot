package com.lifepilot.service;

import com.lifepilot.domain.Note;
import com.lifepilot.repository.NoteRepository;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteFileLinkView;
import com.lifepilot.service.dto.NoteView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteFileService noteFileService;

    @Mock
    private NoteObjectStorage noteObjectStorage;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createsNote() {
        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteView view = noteService.create(new CreateNoteCommand("Meeting notes", "Discuss the MVP scope"));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Meeting notes");
        assertThat(view.content()).isEqualTo("Discuss the MVP scope");
    }

    @Test
    void updatesNote() {
        Note note = Note.create("Old", "Old content");
        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteView updated = noteService.update(note.getId(), new CreateNoteCommand("New", "New content"));

        assertThat(updated.title()).isEqualTo("New");
        assertThat(updated.content()).isEqualTo("New content");
    }

    @Test
    void listsNotesByMostRecentUpdateFirst() {
        Note older = Note.create("Older", "First note");
        Note newer = Note.create("Newer", "Second note");
        ReflectionTestUtils.setField(older, "updatedAt", OffsetDateTime.parse("2026-07-10T09:00:00+08:00"));
        ReflectionTestUtils.setField(newer, "updatedAt", OffsetDateTime.parse("2026-07-10T10:00:00+08:00"));
        when(noteRepository.findAll()).thenReturn(List.of(older, newer));

        List<NoteView> notes = noteService.list();

        assertThat(notes).extracting(NoteView::title).containsExactly("Newer", "Older");
    }

    @Test
    void getsNoteById() {
        Note note = Note.create("Meeting notes", "Discuss the MVP scope");
        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        NoteView found = noteService.get(note.getId());

        assertThat(found.id()).isEqualTo(note.getId());
        assertThat(found.title()).isEqualTo("Meeting notes");
    }

    @Test
    void uploadsFileNote() {
        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(noteFileService.store(any()))
                .thenReturn(new NoteFileService.StoredNoteFile("daily.md", "text/markdown", "notes/daily.md", 128, "# Daily"));

        NoteView view = noteService.upload(null);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("daily");
        assertThat(captor.getValue().getSourceType()).isEqualTo("FILE");
        assertThat(view.originalFilename()).isEqualTo("daily.md");
        assertThat(view.content()).isEqualTo("# Daily");
    }

    @Test
    void replacesFileNoteAndDeletesOldObject() {
        Note note = Note.createFileNote("Old", "old", "old.pdf", "application/pdf", "notes/old.pdf", 16);
        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));
        when(noteFileService.store(any()))
                .thenReturn(new NoteFileService.StoredNoteFile("new.pdf", "application/pdf", "notes/new.pdf", 32, "PDF uploaded"));
        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteView updated = noteService.replaceFile(note.getId(), null);

        assertThat(updated.originalFilename()).isEqualTo("new.pdf");
        verify(noteObjectStorage).deleteObject("notes/old.pdf");
    }

    @Test
    void deletesTextNoteWithoutObjectDeletion() {
        Note note = Note.create("Text", "content");
        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        noteService.delete(note.getId());

        verify(noteRepository).deleteById(note.getId());
        verify(noteObjectStorage, never()).deleteObject(any());
    }

    @Test
    void deletesFileNoteObject() {
        Note note = Note.createFileNote("File", "preview", "a.pdf", "application/pdf", "notes/a.pdf", 16);
        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        noteService.delete(note.getId());

        verify(noteRepository).deleteById(note.getId());
        verify(noteObjectStorage).deleteObject("notes/a.pdf");
    }

    @Test
    void throwsWhenNoteIsMissing() {
        UUID missingId = UUID.randomUUID();
        when(noteRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.get(missingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("note not found");
    }

    @Test
    void createsTemporaryFileLinkForUploadedNote() {
        Note note = Note.createFileNote("Report", "PDF uploaded", "report.pdf", "application/pdf", "notes/report.pdf", 64);
        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));
        when(noteObjectStorage.temporaryUrl("notes/report.pdf", null)).thenReturn("http://minio/report");

        NoteFileLinkView link = noteService.fileLink(note.getId(), false);

        assertThat(link.url()).isEqualTo("http://minio/report");
    }
}
