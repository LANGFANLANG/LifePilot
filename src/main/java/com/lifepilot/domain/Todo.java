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
 * 持久化的待办事项及其生命周期状态。
 */
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TodoStatus status;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected Todo() {
    }

    private Todo(
            UUID id,
            String title,
            String description,
            TodoStatus status,
            OffsetDateTime dueAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueAt = dueAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建处于待处理状态的待办事项。
     *
     * @param title 待办标题
     * @param description 可选的待办详情
     * @param dueAt 可选的截止时间
     * @return 新建的待处理待办事项
     */
    public static Todo create(String title, String description, OffsetDateTime dueAt) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Todo(UUID.randomUUID(), title, description, TodoStatus.PENDING, dueAt, now, now);
    }

    /**
     * 将当前待办标记为已完成并更新修改时间。
     */
    public void complete() {
        status = TodoStatus.COMPLETED;
        updatedAt = OffsetDateTime.now();
    }

    /**
     * 获取待办事项标识。
     *
     * @return 待办事项标识
     */
    public UUID getId() {
        return id;
    }

    /**
     * 获取待办标题。
     *
     * @return 待办标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取可选的待办详情。
     *
     * @return 待办详情；未填写时为 {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取当前待办状态。
     *
     * @return 待办状态
     */
    public TodoStatus getStatus() {
        return status;
    }

    /**
     * 获取可选的截止时间。
     *
     * @return 截止时间；未填写时为 {@code null}
     */
    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取最近修改时间。
     *
     * @return 最近修改时间
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
