package com.lifepilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.controller.dto.SaveDailyReviewRequest;
import com.lifepilot.domain.DailyReviewStatus;
import com.lifepilot.service.DailyReviewService;
import com.lifepilot.service.dto.DailyReviewView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DailyReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class DailyReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DailyReviewService dailyReviewService;

    @Test
    void draftsDailyReview() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 5);
        when(dailyReviewService.draft(date)).thenReturn(review(date, DailyReviewStatus.DRAFT));

        mockMvc.perform(post("/api/reviews/daily/{date}/draft", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void getsDailyReview() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 5);
        when(dailyReviewService.get(date)).thenReturn(review(date, DailyReviewStatus.DRAFT));

        mockMvc.perform(get("/api/reviews/daily/{date}", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedSummary").value("Done"));
    }

    @Test
    void savesDailyReview() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 5);
        when(dailyReviewService.save(eq(date), any())).thenReturn(review(date, DailyReviewStatus.SAVED));

        mockMvc.perform(put("/api/reviews/daily/{date}", date)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SaveDailyReviewRequest(
                                "Done",
                                "Left",
                                "New",
                                "Reflection",
                                "Tomorrow"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SAVED"));
    }

    private static DailyReviewView review(LocalDate date, DailyReviewStatus status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-05T10:00:00+08:00");
        return new DailyReviewView(
                UUID.randomUUID(),
                date,
                "Done",
                "Left",
                "New",
                "Reflection",
                "Tomorrow",
                status,
                now,
                now
        );
    }
}
