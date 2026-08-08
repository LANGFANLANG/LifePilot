package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.ReminderChannel;
import com.lifepilot.domain.ReminderDelivery;
import org.apache.ibatis.annotations.Mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 提醒投递记录的持久化入口。
 */
@Mapper
public interface ReminderDeliveryRepository extends MyBatisRepository<ReminderDelivery> {

    default boolean existsByTodoIdAndReminderAtAndChannel(UUID todoId, OffsetDateTime reminderAt, ReminderChannel channel) {
        return selectCount(Wrappers.lambdaQuery(ReminderDelivery.class)
                .eq(ReminderDelivery::getTodoId, todoId)
                .eq(ReminderDelivery::getReminderAt, reminderAt)
                .eq(ReminderDelivery::getChannel, channel)) > 0;
    }

    default List<ReminderDelivery> findTop20ByOrderByCreatedAtDesc() {
        return selectList(Wrappers.lambdaQuery(ReminderDelivery.class)
                .orderByDesc(ReminderDelivery::getCreatedAt)
                .last("LIMIT 20"));
    }

    default ReminderDelivery save(ReminderDelivery delivery) {
        return save(delivery, ReminderDelivery::getId);
    }
}
