package com.lifepilot.service.dto;

import com.lifepilot.domain.DailyReview;
import com.lifepilot.domain.DailyReviewStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 每日复盘展示数据。
 */
public record DailyReviewView(
        UUID id,
        LocalDate reviewDate,
        String completedSummary,
        String unfinishedSummary,
        String newTasksSummary,
        String reflection,
        String tomorrowPlan,
        DailyReviewStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DailyReviewView from(DailyReview review) {
        return new DailyReviewView(
                review.getId(),
                review.getReviewDate(),
                review.getCompletedSummary(),
                review.getUnfinishedSummary(),
                review.getNewTasksSummary(),
                review.getReflection(),
                review.getTomorrowPlan(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
