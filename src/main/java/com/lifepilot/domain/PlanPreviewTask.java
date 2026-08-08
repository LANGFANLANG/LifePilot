package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AI 计划草案中的单个待办任务。
 */
@TableName("plan_preview_tasks")
public class PlanPreviewTask {

    @TableId
    private UUID id;

    private UUID planPreviewId;

    private String title;

    private String description;

    private OffsetDateTime dueAt;

    private TodoPriority priority;

    private String category;

    private Integer estimatedMinutes;

    private OffsetDateTime plannedStartAt;

    private OffsetDateTime reminderAt;

    private int sortOrder;

    private OffsetDateTime createdAt;

    protected PlanPreviewTask() {
    }

    private PlanPreviewTask(
            UUID id,
            UUID planPreviewId,
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
        this.planPreviewId = planPreviewId;
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
                null,
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

    public UUID getPlanPreviewId() {
        return planPreviewId;
    }

    public void assignToPreview(UUID planPreviewId) {
        this.planPreviewId = planPreviewId;
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
