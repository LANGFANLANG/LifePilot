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
 * 站内提醒投递记录。
 */
@Entity
@Table(name = "reminder_deliveries")
public class ReminderDelivery {

    @Id
    private UUID id;

    @Column(name = "todo_id", nullable = false)
    private UUID todoId;

    @Column(name = "reminder_at", nullable = false)
    private OffsetDateTime reminderAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReminderChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReminderDeliveryStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ReminderDelivery() {
    }

    private ReminderDelivery(
            UUID id,
            UUID todoId,
            OffsetDateTime reminderAt,
            ReminderChannel channel,
            ReminderDeliveryStatus status,
            String message,
            String errorMessage,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.todoId = todoId;
        this.reminderAt = reminderAt;
        this.channel = channel;
        this.status = status;
        this.message = message;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public static ReminderDelivery delivered(UUID todoId, OffsetDateTime reminderAt, String message) {
        return new ReminderDelivery(
                UUID.randomUUID(),
                todoId,
                reminderAt,
                ReminderChannel.IN_APP,
                ReminderDeliveryStatus.DELIVERED,
                message,
                null,
                OffsetDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTodoId() {
        return todoId;
    }

    public OffsetDateTime getReminderAt() {
        return reminderAt;
    }

    public ReminderChannel getChannel() {
        return channel;
    }

    public ReminderDeliveryStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
