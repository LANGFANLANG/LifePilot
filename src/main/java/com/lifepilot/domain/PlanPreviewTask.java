package com.lifepilot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AI 计划草案中的单个待办任务。
 */
@Entity
@Table(name = "plan_preview_tasks")
public class PlanPreviewTask {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TodoPriority priority;

    @Column(length = 80)
    private String category;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "planned_start_at")
    private OffsetDateTime plannedStartAt;

    @Column(name = "reminder_at")
    private OffsetDateTime reminderAt;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected PlanPreviewTask() {
    }

    private PlanPreviewTask(
            UUID id,
            String title,
            String description,
            OffsetDateTime dueAt,
            TodoPriority priority,
            String category,
            Integer estimatedMinutes,
            OffsetDateTime plannedStartAt,
            OffsetDateTime reminderAt,
            int sortOrder,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.priority = priority == null ? TodoPriority.MEDIUM : priority;
        this.category = category;
        this.estimatedMinutes = estimatedMinutes;
        this.plannedStartAt = plannedStartAt;
        this.reminderAt = reminderAt;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public static PlanPreviewTask create(
            String title,
            String description,
            OffsetDateTime dueAt,
            TodoPriority priority,
            String category,
            Integer estimatedMinutes,
            OffsetDateTime plannedStartAt,
            OffsetDateTime reminderAt,
            int sortOrder
    ) {
        return new PlanPreviewTask(
                UUID.randomUUID(),
                title,
                description,
                dueAt,
                priority,
                category,
                estimatedMinutes,
                plannedStartAt,
                reminderAt,
                sortOrder,
                OffsetDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public TodoPriority getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public OffsetDateTime getPlannedStartAt() {
        return plannedStartAt;
    }

    public OffsetDateTime getReminderAt() {
        return reminderAt;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
