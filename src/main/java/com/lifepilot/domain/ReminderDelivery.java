package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 站内提醒投递记录。
 */
@TableName("reminder_deliveries")
public class ReminderDelivery {

    @TableId
    private UUID id;

    private UUID todoId;

    private OffsetDateTime reminderAt;

    private ReminderChannel channel;

    private ReminderDeliveryStatus status;

    private String message;

    private String errorMessage;

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
