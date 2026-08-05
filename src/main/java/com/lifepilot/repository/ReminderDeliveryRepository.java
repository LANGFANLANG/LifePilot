package com.lifepilot.repository;

import com.lifepilot.domain.ReminderChannel;
import com.lifepilot.domain.ReminderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 提醒投递记录的持久化入口。
 */
public interface ReminderDeliveryRepository extends JpaRepository<ReminderDelivery, UUID> {

    boolean existsByTodoIdAndReminderAtAndChannel(UUID todoId, OffsetDateTime reminderAt, ReminderChannel channel);

    List<ReminderDelivery> findTop20ByOrderByCreatedAtDesc();
}
