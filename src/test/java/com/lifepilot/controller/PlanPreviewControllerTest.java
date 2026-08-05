package com.lifepilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.controller.dto.CreatePlanPreviewRequest;
import com.lifepilot.controller.dto.PlanPreviewTaskRequest;
import com.lifepilot.domain.PlanPreviewStatus;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.service.PlanPreviewService;
import com.lifepilot.service.dto.PlanPreviewTaskView;
import com.lifepilot.service.dto.PlanPreviewView;
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

@WebMvcTest(PlanPreviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanPreviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanPreviewService planPreviewService;

    @Test
    void createsPlanPreview() throws Exception {
        UUID id = UUID.randomUUID();
        when(planPreviewService.create(any())).thenReturn(planPreview(id, PlanPreviewStatus.PENDING));

        mockMvc.perform(post("/api/plan-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePlanPreviewRequest(
                                UUID.randomUUID(),
                                "Launch a blog",
                                List.of(new PlanPreviewTaskRequest(
                                        "Choose platform",
                                        null,
                                        null,
                                        TodoPriority.HIGH,
                                        "work",
                                        30,
                                        null,
                                        null
                                ))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getsPlanPreview() throws Exception {
        UUID id = UUID.randomUUID();
        when(planPreviewService.get(id)).thenReturn(planPreview(id, PlanPreviewStatus.PENDING));

        mockMvc.perform(get("/api/plan-previews/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.goal").value("Launch a blog"));
    }

    @Test
    void confirmsPlanPreview() throws Exception {
        UUID id = UUID.randomUUID();
        when(planPreviewService.confirm(id)).thenReturn(List.of(todoView("Choose platform")));

        mockMvc.perform(post("/api/plan-previews/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Choose platform"));
    }

    @Test
    void rejectsPlanPreview() throws Exception {
        UUID id = UUID.randomUUID();
        when(planPreviewService.reject(id)).thenReturn(planPreview(id, PlanPreviewStatus.REJECTED));

        mockMvc.perform(post("/api/plan-previews/{id}/reject", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    private static PlanPreviewView planPreview(UUID id, PlanPreviewStatus status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new PlanPreviewView(
                id,
                UUID.randomUUID(),
                "Launch a blog",
                status,
                List.of(new PlanPreviewTaskView(
                        UUID.randomUUID(),
                        "Choose platform",
                        null,
                        null,
                        TodoPriority.HIGH,
                        "work",
                        30,
                        null,
                        null,
                        0,
                        now
                )),
                now,
                now
        );
    }

    private static TodoView todoView(String title) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new TodoView(
                UUID.randomUUID(),
                title,
                null,
                TodoStatus.PENDING,
                null,
                TodoPriority.HIGH,
                "work",
                30,
                null,
                null,
                null,
                null,
                "ai-plan",
                0,
                now,
                now
        );
    }
}
