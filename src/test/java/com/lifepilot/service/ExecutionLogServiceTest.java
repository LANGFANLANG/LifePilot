package com.lifepilot.service;

import com.lifepilot.domain.ExecutionLog;
import com.lifepilot.domain.ExecutionStatus;
import com.lifepilot.repository.ExecutionLogRepository;
import com.lifepilot.service.dto.ExecutionLogView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionLogServiceTest {

    @Mock
    private ExecutionLogRepository executionLogRepository;

    @InjectMocks
    private ExecutionLogService executionLogService;

    @Test
    void recordsSuccessAction() {
        UUID conversationId = UUID.randomUUID();
        when(executionLogRepository.save(any(ExecutionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExecutionLogView view = executionLogService.recordSuccess(conversationId, "agent.chat", "hello", "hi");

        ArgumentCaptor<ExecutionLog> captor = ArgumentCaptor.forClass(ExecutionLog.class);
        verify(executionLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(view.conversationId()).isEqualTo(conversationId);
        assertThat(view.output()).isEqualTo("hi");
    }

    @Test
    void recordsFailedAction() {
        when(executionLogRepository.save(any(ExecutionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExecutionLogView view = executionLogService.recordFailure(null, "todo.complete", "missing-id", "todo not found");

        ArgumentCaptor<ExecutionLog> captor = ArgumentCaptor.forClass(ExecutionLog.class);
        verify(executionLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ExecutionStatus.FAILURE);
        assertThat(view.errorMessage()).isEqualTo("todo not found");
    }

    @Test
    void listsRecentLogs() {
        ExecutionLog newer = ExecutionLog.success(null, "note.list", "", "[]");
        ExecutionLog older = ExecutionLog.failure(null, "note.get", "missing-id", "note not found");
        when(executionLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newer, older));

        List<ExecutionLogView> logs = executionLogService.listRecent();

        assertThat(logs).extracting(ExecutionLogView::actionType)
                .containsExactly("note.list", "note.get");
    }
}
