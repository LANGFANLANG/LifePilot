package com.lifepilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.controller.dto.CreateNoteRequest;
import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.NoteFileLinkView;
import com.lifepilot.service.dto.NoteView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    @Test
    void createsNote() throws Exception {
        when(noteService.create(any())).thenReturn(noteView(UUID.randomUUID(), "Meeting notes"));

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateNoteRequest("Meeting notes", "Discuss the MVP scope")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Meeting notes"));
    }

    @Test
    void listsNotes() throws Exception {
        when(noteService.list()).thenReturn(List.of(noteView(UUID.randomUUID(), "Meeting notes")));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Meeting notes"));
    }

    @Test
    void getsNote() throws Exception {
        UUID id = UUID.randomUUID();
        when(noteService.get(id)).thenReturn(noteView(id, "Meeting notes"));

        mockMvc.perform(get("/api/notes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void updatesNote() throws Exception {
        UUID id = UUID.randomUUID();
        when(noteService.update(any(), any())).thenReturn(noteView(id, "Updated notes"));

        mockMvc.perform(put("/api/notes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateNoteRequest("Updated notes", "Updated content")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated notes"));
    }

    @Test
    void uploadsNoteFile() throws Exception {
        when(noteService.upload(any())).thenReturn(noteView(UUID.randomUUID(), "daily"));

        mockMvc.perform(multipart("/api/notes/upload")
                        .file("file", "日报".getBytes()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("daily"));
    }

    @Test
    void replacesNoteFile() throws Exception {
        UUID id = UUID.randomUUID();
        when(noteService.replaceFile(any(), any())).thenReturn(noteView(id, "weekly"));

        mockMvc.perform(multipart("/api/notes/{id}/file", id)
                        .file("file", "weekly".getBytes())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("weekly"));
    }

    @Test
    void returnsNoteFileUrl() throws Exception {
        UUID id = UUID.randomUUID();
        when(noteService.fileLink(id, true)).thenReturn(new NoteFileLinkView("http://minio/file"));

        mockMvc.perform(get("/api/notes/{id}/file-url?download=true", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("http://minio/file"));
    }

    @Test
    void deletesNote() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/notes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(noteService).delete(id);
    }

    private static NoteView noteView(UUID id, String title) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new NoteView(id, title, "Discuss the MVP scope", "TEXT", null, null, null, now, now);
    }
}
