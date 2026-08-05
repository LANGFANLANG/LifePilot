package com.lifepilot.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 生成但尚未落库为真实待办的计划草案。
 */
@Entity
@Table(name = "plan_previews")
public class PlanPreview {

    @Id
    private UUID id;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PlanPreviewStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "plan_preview_id", nullable = false)
    @OrderBy("sortOrder ASC")
    private List<PlanPreviewTask> tasks = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
