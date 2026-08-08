package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 生成但尚未落库为真实待办的计划草案。
 */
@TableName("plan_previews")
public class PlanPreview {

    @TableId
    private UUID id;

    private UUID conversationId;

    private String goal;

    private PlanPreviewStatus status;

    @TableField(exist = false)
    private List<PlanPreviewTask> tasks = new ArrayList<>();

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    protected PlanPreview() {
    }

    private PlanPreview(
            UUID id,
            UUID conversationId,
            String goal,
            PlanPreviewStatus status,
            List<PlanPreviewTask> tasks,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.conversationId = conversationId;
        this.goal = goal;
        this.status = status;
        this.tasks = new ArrayList<>(tasks);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PlanPreview create(UUID conversationId, String goal, List<PlanPreviewTask> tasks) {
        if (goal == null || goal.isBlank()) {
            throw new IllegalArgumentException("plan goal is required");
        }
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("plan preview tasks are required");
        }
        OffsetDateTime now = OffsetDateTime.now();
        return new PlanPreview(
                UUID.randomUUID(),
                conversationId,
                goal,
                PlanPreviewStatus.PENDING,
                tasks,
                now,
                now
        );
    }

    public void confirm() {
        if (status != PlanPreviewStatus.PENDING) {
            throw new IllegalArgumentException("plan preview is not pending");
        }
        status = PlanPreviewStatus.CONFIRMED;
        updatedAt = OffsetDateTime.now();
    }

    public void reject() {
        if (status != PlanPreviewStatus.PENDING) {
            throw new IllegalArgumentException("plan preview is not pending");
        }
        status = PlanPreviewStatus.REJECTED;
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public String getGoal() {
        return goal;
    }

    public PlanPreviewStatus getStatus() {
        return status;
    }

    public List<PlanPreviewTask> getTasks() {
        return List.copyOf(tasks);
    }

    public void replaceTasks(List<PlanPreviewTask> tasks) {
        this.tasks = tasks == null ? new ArrayList<>() : new ArrayList<>(tasks);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
