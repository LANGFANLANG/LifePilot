package com.lifepilot.controller;

import com.lifepilot.domain.ReminderChannel;
import com.lifepilot.domain.ReminderDeliveryStatus;
import com.lifepilot.service.ReminderService;
import com.lifepilot.service.dto.ReminderDeliveryView;
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

@WebMvcTest(ReminderController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;

    @Test
    void returnsRecentReminders() throws Exception {
        UUID todoId = UUID.randomUUID();
        when(reminderService.listRecent()).thenReturn(List.of(new ReminderDeliveryView(
                UUID.randomUUID(),
                todoId,
                OffsetDateTime.parse("2026-08-05T09:55:00+08:00"),
                ReminderChannel.IN_APP,
                ReminderDeliveryStatus.DELIVERED,
                "该做「Write report」了",
                null,
                OffsetDateTime.parse("2026-08-05T10:00:00+08:00")
        )));

        mockMvc.perform(get("/api/reminders/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].todoId").value(todoId.toString()))
                .andExpect(jsonPath("$.data[0].message").value("该做「Write report」了"));
    }
}
