package com.lifepilot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 某一天的执行复盘。
 */
@Entity
@Table(name = "daily_reviews")
public class DailyReview {

    @Id
    private UUID id;

    @Column(name = "review_date", nullable = false, unique = true)
    private LocalDate reviewDate;

    @Column(name = "completed_summary", columnDefinition = "TEXT")
    private String completedSummary;

    @Column(name = "unfinished_summary", columnDefinition = "TEXT")
    private String unfinishedSummary;

    @Column(name = "new_tasks_summary", columnDefinition = "TEXT")
    private String newTasksSummary;

    @Column(columnDefinition = "TEXT")
    private String reflection;

    @Column(name = "tomorrow_plan", columnDefinition = "TEXT")
    private String tomorrowPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DailyReviewStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
