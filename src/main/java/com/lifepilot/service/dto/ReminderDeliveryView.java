package com.lifepilot.service.dto;

import com.lifepilot.domain.ReminderChannel;
import com.lifepilot.domain.ReminderDelivery;
import com.lifepilot.domain.ReminderDeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 提醒投递记录展示数据。
 */
public record ReminderDeliveryView(
        UUID id,
        UUID todoId,
        OffsetDateTime reminderAt,
        ReminderChannel channel,
        ReminderDeliveryStatus status,
        String message,
        String errorMessage,
        OffsetDateTime createdAt
) {

    public static ReminderDeliveryView from(ReminderDelivery delivery) {
        return new ReminderDeliveryView(
                delivery.getId(),
                delivery.getTodoId(),
                delivery.getReminderAt(),
                delivery.getChannel(),
                delivery.getStatus(),
                delivery.getMessage(),
                delivery.getErrorMessage(),
                delivery.getCreatedAt()
        );
    }
}
