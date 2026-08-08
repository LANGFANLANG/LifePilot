package com.lifepilot.service;

import com.lifepilot.domain.PlanPreview;
import com.lifepilot.domain.PlanPreviewStatus;
import com.lifepilot.domain.PlanPreviewTask;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.PlanPreviewRepository;
import com.lifepilot.repository.PlanPreviewTaskRepository;
import com.lifepilot.service.dto.CreatePlanPreviewCommand;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.PlanPreviewView;
import com.lifepilot.service.dto.TodoView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanPreviewServiceTest {

    @Mock
    private PlanPreviewRepository planPreviewRepository;

    @Mock
    private PlanPreviewTaskRepository planPreviewTaskRepository;

    @Mock
    private TodoService todoService;

    @InjectMocks
    private PlanPreviewService planPreviewService;

    @Test
    void createsPreviewWithoutCreatingTodos() {
        when(planPreviewRepository.save(any(PlanPreview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlanPreviewView view = planPreviewService.create(new CreatePlanPreviewCommand(
                UUID.randomUUID(),
                "Launch a blog",
                List.of(todoCommand("Choose platform"))
        ));

        assertThat(view.status()).isEqualTo(PlanPreviewStatus.PENDING);
        assertThat(view.tasks()).hasSize(1);
        verify(todoService, never()).create(any());
    }

    @Test
    void confirmsPreviewByCreatingTodos() {
        UUID previewId = UUID.randomUUID();
        PlanPreview preview = PlanPreview.create(
                UUID.randomUUID(),
                "Launch a blog",
                List.of(task("Choose platform"), task("Write first post"))
        );
        when(planPreviewRepository.findById(previewId)).thenReturn(Optional.of(preview));
        when(planPreviewTaskRepository.findByPlanPreviewIdOrderBySortOrderAsc(previewId))
                .thenReturn(preview.getTasks());
        when(todoService.create(any())).thenReturn(todoView("created"));
        when(planPreviewRepository.save(any(PlanPreview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TodoView> todos = planPreviewService.confirm(previewId);

        ArgumentCaptor<CreateTodoCommand> captor = ArgumentCaptor.forClass(CreateTodoCommand.class);
        verify(todoService, times(2)).create(captor.capture());
        assertThat(captor.getAllValues()).extracting(CreateTodoCommand::source)
                .containsExactly("ai-plan", "ai-plan");
        assertThat(preview.getStatus()).isEqualTo(PlanPreviewStatus.CONFIRMED);
        assertThat(todos).hasSize(2);
    }

    @Test
    void rejectsPreviewWithoutCreatingTodos() {
        UUID previewId = UUID.randomUUID();
        PlanPreview preview = PlanPreview.create(UUID.randomUUID(), "Launch a blog", List.of(task("Choose platform")));
        when(planPreviewRepository.findById(previewId)).thenReturn(Optional.of(preview));
        when(planPreviewRepository.save(preview)).thenReturn(preview);

        PlanPreviewView view = planPreviewService.reject(previewId);

        assertThat(view.status()).isEqualTo(PlanPreviewStatus.REJECTED);
        verify(todoService, never()).create(any());
    }

    @Test
    void throwsWhenConfirmingMissingPreview() {
        UUID previewId = UUID.randomUUID();
        when(planPreviewRepository.findById(previewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planPreviewService.confirm(previewId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plan preview not found");
    }

    @Test
    void throwsWhenConfirmingNonPendingPreview() {
        UUID previewId = UUID.randomUUID();
        PlanPreview preview = PlanPreview.create(UUID.randomUUID(), "Launch a blog", List.of(task("Choose platform")));
        preview.reject();
        when(planPreviewRepository.findById(previewId)).thenReturn(Optional.of(preview));
        when(planPreviewTaskRepository.findByPlanPreviewIdOrderBySortOrderAsc(previewId))
                .thenReturn(preview.getTasks());

        assertThatThrownBy(() -> planPreviewService.confirm(previewId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plan preview is not pending");
    }

    private static CreateTodoCommand todoCommand(String title) {
        return new CreateTodoCommand(title, null, null, TodoPriority.HIGH, "work", 30, null, null, null, "ai");
    }

    private static PlanPreviewTask task(String title) {
        return PlanPreviewTask.create(title, null, null, TodoPriority.HIGH, "work", 30, null, null, 0);
    }

    private static TodoView todoView(String title) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new TodoView(
                UUID.randomUUID(),
                title,
                null,
                TodoStatus.PENDING,
                null,
                TodoPriority.HIGH,
                "work",
                30,
                null,
                null,
                null,
                null,
                "ai-plan",
                0,
                now,
                now
        );
    }
}
