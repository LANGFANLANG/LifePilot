package com.lifepilot.service;

import com.lifepilot.domain.ReminderChannel;
import com.lifepilot.domain.ReminderDelivery;
import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.ReminderDeliveryRepository;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.ReminderDeliveryView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private ReminderDeliveryRepository reminderDeliveryRepository;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void createsOneDeliveryPerDueReminder() {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-05T10:00:00+08:00");
        Todo todo = todo("Write report", "2026-08-05T09:55:00+08:00");
        when(todoRepository.findByStatusAndReminderAtLessThanEqual(TodoStatus.PENDING, now))
                .thenReturn(List.of(todo));
        when(reminderDeliveryRepository.existsByTodoIdAndReminderAtAndChannel(
                todo.getId(),
                todo.getReminderAt(),
                ReminderChannel.IN_APP
        )).thenReturn(false);
        when(reminderDeliveryRepository.save(any(ReminderDelivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReminderDeliveryView> deliveries = reminderService.deliverDueReminders(now);

        ArgumentCaptor<ReminderDelivery> captor = ArgumentCaptor.forClass(ReminderDelivery.class);
        verify(reminderDeliveryRepository).save(captor.capture());
        assertThat(captor.getValue().getTodoId()).isEqualTo(todo.getId());
        assertThat(captor.getValue().getMessage()).isEqualTo("该做「Write report」了");
        assertThat(deliveries).hasSize(1);
    }

    @Test
    void skipsAlreadyDeliveredReminder() {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-05T10:00:00+08:00");
        Todo todo = todo("Write report", "2026-08-05T09:55:00+08:00");
        when(todoRepository.findByStatusAndReminderAtLessThanEqual(TodoStatus.PENDING, now))
                .thenReturn(List.of(todo));
        when(reminderDeliveryRepository.existsByTodoIdAndReminderAtAndChannel(
                todo.getId(),
                todo.getReminderAt(),
                ReminderChannel.IN_APP
        )).thenReturn(true);

        List<ReminderDeliveryView> deliveries = reminderService.deliverDueReminders(now);

        verify(reminderDeliveryRepository, never()).save(any());
        assertThat(deliveries).isEmpty();
    }

    @Test
    void listsRecentDeliveries() {
        ReminderDelivery delivery = ReminderDelivery.delivered(
                java.util.UUID.randomUUID(),
                OffsetDateTime.parse("2026-08-05T09:55:00+08:00"),
                "该做「Write report」了"
        );
        when(reminderDeliveryRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(delivery));

        List<ReminderDeliveryView> recent = reminderService.listRecent();

        assertThat(recent).hasSize(1);
        assertThat(recent.getFirst().message()).isEqualTo("该做「Write report」了");
    }

    private static Todo todo(String title, String reminderAt) {
        return Todo.create(
                title,
                null,
                null,
                TodoPriority.MEDIUM,
                "work",
                30,
                null,
                OffsetDateTime.parse(reminderAt),
                null,
                "manual"
        );
    }
}
