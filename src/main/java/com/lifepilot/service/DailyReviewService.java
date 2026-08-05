package com.lifepilot.service;

import com.lifepilot.agent.ReviewDraftClient;
import com.lifepilot.agent.dto.ReviewDraft;
import com.lifepilot.agent.dto.ReviewDraftInput;
import com.lifepilot.domain.DailyReview;
import com.lifepilot.domain.DailyReviewStatus;
import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.DailyReviewRepository;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.DailyReviewView;
import com.lifepilot.service.dto.SaveDailyReviewCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 生成、读取并保存每日执行复盘。
 */
@Service
public class DailyReviewService {

    private final DailyReviewRepository dailyReviewRepository;
    private final TodoRepository todoRepository;
    private final ReviewDraftClient reviewDraftClient;
    private final Clock clock;

    public DailyReviewService(
            DailyReviewRepository dailyReviewRepository,
            TodoRepository todoRepository,
            ReviewDraftClient reviewDraftClient,
            Clock clock
    ) {
        this.dailyReviewRepository = dailyReviewRepository;
        this.todoRepository = todoRepository;
        this.reviewDraftClient = reviewDraftClient;
        this.clock = clock;
    }

    @Transactional
    public DailyReviewView draft(LocalDate date) {
        ReviewDraft deterministicDraft = deterministicDraft(date);
        ReviewDraft draft = tryAiDraft(date, deterministicDraft);
        DailyReview review = dailyReviewRepository.findByReviewDate(date)
                .orElseGet(() -> DailyReview.draft(
                        date,
                        draft.completedSummary(),
                        draft.unfinishedSummary(),
                        draft.newTasksSummary(),
                        draft.reflection(),
                        draft.tomorrowPlan()
                ));
        review.update(
                draft.completedSummary(),
                draft.unfinishedSummary(),
                draft.newTasksSummary(),
                draft.reflection(),
                draft.tomorrowPlan(),
                DailyReviewStatus.DRAFT
        );
        return DailyReviewView.from(dailyReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public DailyReviewView get(LocalDate date) {
        return DailyReviewView.from(dailyReviewRepository.findByReviewDate(date)
                .orElseThrow(() -> new IllegalArgumentException("daily review not found")));
    }

    @Transactional
    public DailyReviewView save(LocalDate date, SaveDailyReviewCommand command) {
        DailyReview review = dailyReviewRepository.findByReviewDate(date)
                .orElseGet(() -> DailyReview.draft(date, "", "", "", "", ""));
        review.update(
                command.completedSummary(),
                command.unfinishedSummary(),
                command.newTasksSummary(),
                command.reflection(),
                command.tomorrowPlan(),
                DailyReviewStatus.SAVED
        );
        return DailyReviewView.from(dailyReviewRepository.save(review));
    }

    private ReviewDraft tryAiDraft(LocalDate date, ReviewDraft fallback) {
        try {
            ReviewDraftInput input = buildInput(date);
            return reviewDraftClient.draft(input);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private ReviewDraft deterministicDraft(LocalDate date) {
        ReviewDraftInput input = buildInput(date);
        return new ReviewDraft(
                summarize("已完成", input.completedTasks()),
                summarize("未完成", input.unfinishedTasks()),
                summarize("新增任务", input.newTasks()),
                "今天的执行记录已整理，可以补充阻碍、状态和注意力变化。",
                "从未完成任务中挑选 1-3 项，安排到明天的具体时间。"
        );
    }

    private ReviewDraftInput buildInput(LocalDate date) {
        OffsetDateTime start = date.atStartOfDay(zone()).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(zone()).toOffsetDateTime();
        List<String> completed = todoRepository.findByCompletedAtBetween(start, end).stream()
                .map(Todo::getTitle)
                .toList();
        List<String> unfinished = todoRepository.findByStatusAndPlannedStartAtBetween(TodoStatus.PENDING, start, end)
                .stream()
                .map(Todo::getTitle)
                .toList();
        List<String> created = todoRepository.findByCreatedAtBetween(start, end).stream()
                .map(Todo::getTitle)
                .toList();
        return new ReviewDraftInput(date, completed, unfinished, created);
    }

    private String summarize(String label, List<String> tasks) {
        if (tasks.isEmpty()) {
            return label + "：暂无。";
        }
        return label + "：" + String.join("；", tasks) + "。";
    }

    private ZoneId zone() {
        return clock.getZone();
    }
}
