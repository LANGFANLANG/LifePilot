package com.lifepilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.controller.dto.CreateTodoRequest;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.TodoView;
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

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @Test
    void createsTodo() throws Exception {
        TodoView todo = todoView(UUID.randomUUID(), "Buy milk", TodoStatus.PENDING);
        when(todoService.create(any())).thenReturn(todo);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTodoRequest("Buy milk", "2 bottles", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Buy milk"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void listsTodos() throws Exception {
        when(todoService.list()).thenReturn(List.of(
                todoView(UUID.randomUUID(), "Buy milk", TodoStatus.PENDING)
        ));

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Buy milk"));
    }

    @Test
    void completesTodo() throws Exception {
        UUID id = UUID.randomUUID();
        when(todoService.complete(id)).thenReturn(todoView(id, "Buy milk", TodoStatus.COMPLETED));

        mockMvc.perform(post("/api/todos/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    private static TodoView todoView(UUID id, String title, TodoStatus status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new TodoView(id, title, "2 bottles", status, null, now, now);
    }
}
