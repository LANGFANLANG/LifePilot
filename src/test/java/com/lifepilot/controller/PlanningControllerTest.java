package com.lifepilot.controller;

import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.service.PlanningService;
import com.lifepilot.service.dto.TodayPlanItemView;
import com.lifepilot.service.dto.TodayPlanView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanningController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanningService planningService;

    @Test
    void returnsTodayPlan() throws Exception {
        when(planningService.today()).thenReturn(todayPlan());

        mockMvc.perform(get("/api/planning/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.focus[0].title").value("Write launch plan"));
    }

    private static TodayPlanView todayPlan() {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-05T10:00:00+08:00");
        TodayPlanItemView item = new TodayPlanItemView(
                UUID.randomUUID(),
                "Write launch plan",
                null,
                TodoStatus.PENDING,
                null,
                TodoPriority.HIGH,
                "work",
                45,
                now.plusHours(1),
                null,
                false,
                1
        );
        return new TodayPlanView(now, List.of(item), List.of(item), List.of(), 0, 1, 45);
    }
}
