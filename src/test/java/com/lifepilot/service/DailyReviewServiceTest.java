package com.lifepilot.service;

import com.lifepilot.agent.ReviewDraftClient;
import com.lifepilot.agent.dto.ReviewDraft;
import com.lifepilot.agent.dto.ReviewDraftInput;
import com.lifepilot.domain.DailyReview;
import com.lifepilot.domain.DailyReviewStatus;
import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.DailyReviewRepository;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.DailyReviewView;
import com.lifepilot.service.dto.SaveDailyReviewCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReviewServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-05T02:00:00Z"), ZoneId.of("Asia/Shanghai"));
    private final LocalDate date = LocalDate.of(2026, 8, 5);

    @Mock
    private DailyReviewRepository dailyReviewRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private ReviewDraftClient reviewDraftClient;

    @Test
    void draftsReviewFromTodoHistory() {
        DailyReviewService service = service();
        stubTodoInputs();
        when(reviewDraftClient.draft(any())).thenThrow(new IllegalStateException("ai unavailable"));
        when(dailyReviewRepository.findByReviewDate(date)).thenReturn(Optional.empty());
        when(dailyReviewRepository.save(any(DailyReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyReviewView view = service.draft(date);

        assertThat(view.status()).isEqualTo(DailyReviewStatus.DRAFT);
        assertThat(view.completedSummary()).contains("Write report");
        assertThat(view.unfinishedSummary()).contains("Plan launch");
        assertThat(view.newTasksSummary()).contains("Review notes");
    }

    @Test
    void usesAiDraftWhenAvailable() {
        DailyReviewService service = service();
        stubTodoInputs();
        when(reviewDraftClient.draft(any(ReviewDraftInput.class))).thenReturn(new ReviewDraft(
                "AI completed",
                "AI unfinished",
                "AI new",
                "AI reflection",
                "AI tomorrow"
        ));
        when(dailyReviewRepository.findByReviewDate(date)).thenReturn(Optional.empty());
        when(dailyReviewRepository.save(any(DailyReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyReviewView view = service.draft(date);

        assertThat(view.completedSummary()).isEqualTo("AI completed");
        assertThat(view.tomorrowPlan()).isEqualTo("AI tomorrow");
    }

    @Test
    void savesEditedReview() {
        DailyReviewService service = service();
        when(dailyReviewRepository.findByReviewDate(date)).thenReturn(Optional.empty());
        when(dailyReviewRepository.save(any(DailyReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyReviewView view = service.save(date, new SaveDailyReviewCommand(
                "Done",
                "Left",
                "New",
                "Reflection",
                "Tomorrow"
        ));

        ArgumentCaptor<DailyReview> captor = ArgumentCaptor.forClass(DailyReview.class);
        verify(dailyReviewRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DailyReviewStatus.SAVED);
        assertThat(view.reflection()).isEqualTo("Reflection");
    }

    @Test
    void getsExistingReview() {
        DailyReviewService service = service();
        DailyReview review = DailyReview.draft(date, "Done", "Left", "New", "Reflection", "Tomorrow");
        when(dailyReviewRepository.findByReviewDate(date)).thenReturn(Optional.of(review));

        DailyReviewView view = service.get(date);

        assertThat(view.completedSummary()).isEqualTo("Done");
    }

    @Test
    void throwsWhenReviewMissing() {
        DailyReviewService service = service();
        when(dailyReviewRepository.findByReviewDate(date)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(date))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("daily review not found");
    }

    private DailyReviewService service() {
        return new DailyReviewService(dailyReviewRepository, todoRepository, reviewDraftClient, clock);
    }

    private void stubTodoInputs() {
        OffsetDateTime start = OffsetDateTime.parse("2026-08-05T00:00:00+08:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-08-06T00:00:00+08:00");
        when(todoRepository.findByCompletedAtBetween(start, end)).thenReturn(List.of(todo("Write report")));
        when(todoRepository.findByStatusAndPlannedStartAtBetween(TodoStatus.PENDING, start, end))
                .thenReturn(List.of(todo("Plan launch")));
        when(todoRepository.findByCreatedAtBetween(start, end)).thenReturn(List.of(todo("Review notes")));
    }

    private static Todo todo(String title) {
        return Todo.create(title, null, null, TodoPriority.MEDIUM, "work", 30, null, null, null, "manual");
    }
}
