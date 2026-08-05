package com.lifepilot.service;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.TodayPlanItemView;
import com.lifepilot.service.dto.TodayPlanView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 将待办事项整理成确定性的今日执行计划。
 */
@Service
public class PlanningService {

    private final TodoRepository todoRepository;
    private final Clock clock;

    public PlanningService(TodoRepository todoRepository, Clock clock) {
        this.todoRepository = todoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TodayPlanView today() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        List<Todo> pending = todoRepository.findByStatus(TodoStatus.PENDING);
        List<Todo> ranked = pending.stream()
                .sorted(rankingComparator(now))
                .toList();
        List<TodayPlanItemView> rankedViews = toRankedViews(ranked, now);
        List<TodayPlanItemView> focus = rankedViews.stream()
                .limit(3)
                .toList();
        List<TodayPlanItemView> timeline = rankedViews.stream()
                .filter(item -> item.plannedStartAt() != null)
                .sorted(Comparator.comparing(TodayPlanItemView::plannedStartAt))
                .toList();
        List<TodayPlanItemView> inbox = rankedViews.stream()
                .filter(item -> item.plannedStartAt() == null && item.dueAt() == null)
                .toList();
        int overdueCount = (int) rankedViews.stream().filter(TodayPlanItemView::overdue).count();
        int estimatedMinutes = pending.stream()
                .map(Todo::getEstimatedMinutes)
                .filter(minutes -> minutes != null)
                .mapToInt(Integer::intValue)
                .sum();
        return new TodayPlanView(now, focus, timeline, inbox, overdueCount, pending.size(), estimatedMinutes);
    }

    private List<TodayPlanItemView> toRankedViews(List<Todo> ranked, OffsetDateTime now) {
        return IntStream.range(0, ranked.size())
                .mapToObj(index -> TodayPlanItemView.from(ranked.get(index), isOverdue(ranked.get(index), now), index + 1))
                .toList();
    }

    private Comparator<Todo> rankingComparator(OffsetDateTime now) {
        return Comparator
                .comparing((Todo todo) -> !isOverdue(todo, now))
                .thenComparing((Todo todo) -> -priorityWeight(todo.getPriority()))
                .thenComparing(todo -> todo.getPlannedStartAt() == null ? OffsetDateTime.MAX : todo.getPlannedStartAt())
                .thenComparing(todo -> todo.getDueAt() == null ? OffsetDateTime.MAX : todo.getDueAt())
                .thenComparing((Todo todo) -> -ageHours(todo, now))
                .thenComparing(Todo::getCreatedAt);
    }

    private boolean isOverdue(Todo todo, OffsetDateTime now) {
        return todo.getDueAt() != null && todo.getDueAt().isBefore(now);
    }

    private int priorityWeight(TodoPriority priority) {
        return switch (priority == null ? TodoPriority.MEDIUM : priority) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private long ageHours(Todo todo, OffsetDateTime now) {
        return Math.max(0, Duration.between(todo.getCreatedAt(), now).toHours());
    }
}
