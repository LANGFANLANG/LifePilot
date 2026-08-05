package com.lifepilot.service;

import com.lifepilot.domain.ReminderChannel;
import com.lifepilot.domain.ReminderDelivery;
import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.ReminderDeliveryRepository;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.ReminderDeliveryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 扫描并记录到期的站内提醒。
 */
@Service
public class ReminderService {

    private final TodoRepository todoRepository;
    private final ReminderDeliveryRepository reminderDeliveryRepository;

    public ReminderService(TodoRepository todoRepository, ReminderDeliveryRepository reminderDeliveryRepository) {
        this.todoRepository = todoRepository;
        this.reminderDeliveryRepository = reminderDeliveryRepository;
    }

    @Transactional
    public List<ReminderDeliveryView> deliverDueReminders(OffsetDateTime now) {
        return todoRepository.findByStatusAndReminderAtLessThanEqual(TodoStatus.PENDING, now).stream()
                .filter(todo -> todo.getReminderAt() != null)
                .filter(this::hasNotBeenDelivered)
                .map(this::deliver)
                .map(ReminderDeliveryView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderDeliveryView> listRecent() {
        return reminderDeliveryRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(ReminderDeliveryView::from)
                .toList();
    }

    private boolean hasNotBeenDelivered(Todo todo) {
        return !reminderDeliveryRepository.existsByTodoIdAndReminderAtAndChannel(
                todo.getId(),
                todo.getReminderAt(),
                ReminderChannel.IN_APP
        );
    }

    private ReminderDelivery deliver(Todo todo) {
        String message = "该做「" + todo.getTitle() + "」了";
        return reminderDeliveryRepository.save(ReminderDelivery.delivered(todo.getId(), todo.getReminderAt(), message));
    }
}
