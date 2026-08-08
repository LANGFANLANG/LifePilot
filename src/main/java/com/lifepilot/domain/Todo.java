package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 持久化的待办事项及其生命周期状态。
 */
@TableName("todos")
public class Todo {

    @TableId
    private UUID id;

    private String title;

    private String description;

    private TodoStatus status;

    private OffsetDateTime dueAt;

    private TodoPriority priority;

    private String category;

    private Integer estimatedMinutes;

    private OffsetDateTime plannedStartAt;

    private OffsetDateTime reminderAt;

    private OffsetDateTime completedAt;

    private UUID parentTodoId;

    private String source;

    private int postponementCount;

    private OffsetDateTime createdAt;

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
            TodoPriority priority,
            String category,
            Integer estimatedMinutes,
            OffsetDateTime plannedStartAt,
            OffsetDateTime reminderAt,
            OffsetDateTime completedAt,
            UUID parentTodoId,
            String source,
            int postponementCount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueAt = dueAt;
        this.priority = priority == null ? TodoPriority.MEDIUM : priority;
        this.category = category;
        this.estimatedMinutes = estimatedMinutes;
        this.plannedStartAt = plannedStartAt;
        this.reminderAt = reminderAt;
        this.completedAt = completedAt;
        this.parentTodoId = parentTodoId;
        this.source = source == null || source.isBlank() ? "manual" : source;
        this.postponementCount = postponementCount;
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
        return create(title, description, dueAt, TodoPriority.MEDIUM, null, null, null, null, null, "manual");
    }

    public static Todo create(
            String title,
            String description,
            OffsetDateTime dueAt,
            TodoPriority priority,
            String category,
            Integer estimatedMinutes,
            OffsetDateTime plannedStartAt,
            OffsetDateTime reminderAt,
            UUID parentTodoId,
            String source
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Todo(
                UUID.randomUUID(),
                title,
                description,
                TodoStatus.PENDING,
                dueAt,
                priority,
                category,
                estimatedMinutes,
                plannedStartAt,
                reminderAt,
                null,
                parentTodoId,
                source,
                0,
                now,
                now
        );
    }

    /**
     * 将当前待办标记为已完成并更新修改时间。
     */
    public void complete() {
        status = TodoStatus.COMPLETED;
        OffsetDateTime now = OffsetDateTime.now();
        completedAt = now;
        updatedAt = now;
    }

    public void update(
            String title,
            String description,
            OffsetDateTime dueAt,
            TodoPriority priority,
            String category,
            Integer estimatedMinutes,
            OffsetDateTime plannedStartAt,
            OffsetDateTime reminderAt,
            UUID parentTodoId,
            String source
    ) {
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.priority = priority == null ? TodoPriority.MEDIUM : priority;
        this.category = category;
        this.estimatedMinutes = estimatedMinutes;
        this.plannedStartAt = plannedStartAt;
        this.reminderAt = reminderAt;
        this.parentTodoId = parentTodoId;
        this.source = source == null || source.isBlank() ? "manual" : source;
        this.updatedAt = OffsetDateTime.now();
    }

    public void postpone() {
        postponementCount++;
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

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public UUID getParentTodoId() {
        return parentTodoId;
    }

    public String getSource() {
        return source;
    }

    public int getPostponementCount() {
        return postponementCount;
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
