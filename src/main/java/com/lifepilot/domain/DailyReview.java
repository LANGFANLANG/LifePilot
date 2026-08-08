package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 某一天的执行复盘。
 */
@TableName("daily_reviews")
public class DailyReview {

    @TableId
    private UUID id;

    private LocalDate reviewDate;

    private String completedSummary;

    private String unfinishedSummary;

    private String newTasksSummary;

    private String reflection;

    private String tomorrowPlan;

    private DailyReviewStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    protected DailyReview() {
    }

    private DailyReview(
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
        this.id = id;
        this.reviewDate = reviewDate;
        this.completedSummary = completedSummary;
        this.unfinishedSummary = unfinishedSummary;
        this.newTasksSummary = newTasksSummary;
        this.reflection = reflection;
        this.tomorrowPlan = tomorrowPlan;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DailyReview draft(
            LocalDate reviewDate,
            String completedSummary,
            String unfinishedSummary,
            String newTasksSummary,
            String reflection,
            String tomorrowPlan
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        return new DailyReview(
                UUID.randomUUID(),
                reviewDate,
                completedSummary,
                unfinishedSummary,
                newTasksSummary,
                reflection,
                tomorrowPlan,
                DailyReviewStatus.DRAFT,
                now,
                now
        );
    }

    public void update(
            String completedSummary,
            String unfinishedSummary,
            String newTasksSummary,
            String reflection,
            String tomorrowPlan,
            DailyReviewStatus status
    ) {
        this.completedSummary = completedSummary;
        this.unfinishedSummary = unfinishedSummary;
        this.newTasksSummary = newTasksSummary;
        this.reflection = reflection;
        this.tomorrowPlan = tomorrowPlan;
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public String getCompletedSummary() {
        return completedSummary;
    }

    public String getUnfinishedSummary() {
        return unfinishedSummary;
    }

    public String getNewTasksSummary() {
        return newTasksSummary;
    }

    public String getReflection() {
        return reflection;
    }

    public String getTomorrowPlan() {
        return tomorrowPlan;
    }

    public DailyReviewStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
