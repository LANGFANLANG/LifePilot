package com.lifepilot.service;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.TodayPlanView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-05T02:00:00Z"),
            ZoneId.of("Asia/Shanghai")
    );

    @Mock
    private TodoRepository todoRepository;

    @Test
    void ranksOverdueHighPriorityAndScheduledTasksIntoFocus() {
        PlanningService planningService = new PlanningService(todoRepository, clock);
        Todo lowFuture = todo("Low future", TodoPriority.LOW, "2026-08-08T10:00:00+08:00", null, 20);
        Todo highFuture = todo("High future", TodoPriority.HIGH, "2026-08-08T10:00:00+08:00", null, 30);
        Todo overdue = todo("Overdue", TodoPriority.LOW, "2026-08-04T10:00:00+08:00", null, 10);
        Todo scheduled = todo("Scheduled", TodoPriority.MEDIUM, null, "2026-08-05T11:00:00+08:00", 40);
        when(todoRepository.findByStatus(TodoStatus.PENDING)).thenReturn(List.of(lowFuture, highFuture, overdue, scheduled));

        TodayPlanView today = planningService.today();

        assertThat(today.focus()).extracting("title")
                .containsExactly("Overdue", "High future", "Scheduled");
        assertThat(today.overdueCount()).isEqualTo(1);
        assertThat(today.pendingCount()).isEqualTo(4);
        assertThat(today.estimatedMinutes()).isEqualTo(100);
    }

    @Test
    void buildsTimelineAndInbox() {
        PlanningService planningService = new PlanningService(todoRepository, clock);
        Todo later = todo("Later", TodoPriority.MEDIUM, null, "2026-08-05T15:00:00+08:00", 30);
        Todo earlier = todo("Earlier", TodoPriority.MEDIUM, null, "2026-08-05T09:00:00+08:00", 30);
        Todo inbox = todo("Inbox", TodoPriority.MEDIUM, null, null, null);
        when(todoRepository.findByStatus(TodoStatus.PENDING)).thenReturn(List.of(later, inbox, earlier));

        TodayPlanView today = planningService.today();

        assertThat(today.timeline()).extracting("title")
                .containsExactly("Earlier", "Later");
        assertThat(today.inbox()).extracting("title")
                .containsExactly("Inbox");
    }

    @Test
    void completedTodosAreExcludedByRepositoryQuery() {
        PlanningService planningService = new PlanningService(todoRepository, clock);
        when(todoRepository.findByStatus(TodoStatus.PENDING)).thenReturn(List.of());

        TodayPlanView today = planningService.today();

        assertThat(today.focus()).isEmpty();
        assertThat(today.pendingCount()).isZero();
    }

    private static Todo todo(
            String title,
            TodoPriority priority,
            String dueAt,
            String plannedStartAt,
            Integer estimatedMinutes
    ) {
        return Todo.create(
                title,
                null,
                dueAt == null ? null : OffsetDateTime.parse(dueAt),
                priority,
                "work",
                estimatedMinutes,
                plannedStartAt == null ? null : OffsetDateTime.parse(plannedStartAt),
                null,
                null,
                "manual"
        );
    }
}
