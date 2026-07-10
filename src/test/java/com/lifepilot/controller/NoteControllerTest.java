package com.lifepilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.controller.dto.CreateNoteRequest;
import com.lifepilot.service.NoteService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private static NoteView noteView(UUID id, String title) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new NoteView(id, title, "Discuss the MVP scope", now, now);
    }
}
