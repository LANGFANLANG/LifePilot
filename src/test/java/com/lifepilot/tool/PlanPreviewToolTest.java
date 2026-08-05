package com.lifepilot.tool;

import com.lifepilot.agent.PlanPreviewActionContext;
import com.lifepilot.domain.PlanPreviewStatus;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.service.ExecutionLogService;
import com.lifepilot.service.PlanPreviewService;
import com.lifepilot.service.dto.CreatePlanPreviewCommand;
import com.lifepilot.service.dto.PlanPreviewTaskView;
import com.lifepilot.service.dto.PlanPreviewView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanPreviewToolTest {

    @Mock
    private PlanPreviewService planPreviewService;

    @Mock
    private ExecutionLogService executionLogService;

    @Mock
    private PlanPreviewActionContext planPreviewActionContext;

    @InjectMocks
    private PlanPreviewTool planPreviewTool;

    @Test
    void createsPlanPreviewThroughService() {
        PlanPreviewView preview = preview();
        when(planPreviewService.create(any())).thenReturn(preview);

        ToolResult result = planPreviewTool.createPlanPreview("Launch a blog", List.of(
                new PlanPreviewTaskInput("Choose platform", null, null, TodoPriority.HIGH, "work", 30, null, null)
        ));

        ArgumentCaptor<CreatePlanPreviewCommand> captor = ArgumentCaptor.forClass(CreatePlanPreviewCommand.class);
        verify(planPreviewService).create(captor.capture());
        verify(planPreviewActionContext).recordPlanPreview(preview.id(), "确认计划：" + preview.goal());
        verify(executionLogService).recordSuccess(null, "plan-preview.create", "Launch a blog", preview.toString());
        assertThat(captor.getValue().tasks()).hasSize(1);
        assertThat(captor.getValue().tasks().getFirst().source()).isEqualTo("ai-preview");
        assertThat(result).isEqualTo(ToolResult.success("plan preview created", preview));
    }

    @Test
    void getsPlanPreviewThroughService() {
        PlanPreviewView preview = preview();
        when(planPreviewService.get(preview.id())).thenReturn(preview);

        ToolResult result = planPreviewTool.getPlanPreview(preview.id());

        verify(planPreviewService).get(preview.id());
        verify(executionLogService).recordSuccess(null, "plan-preview.get", preview.id().toString(), preview.toString());
        assertThat(result).isEqualTo(ToolResult.success("plan preview found", preview));
    }

    @Test
    void recordsFailureAndRethrows() {
        when(planPreviewService.create(any())).thenThrow(new IllegalArgumentException("plan preview tasks are required"));

        assertThatThrownBy(() -> planPreviewTool.createPlanPreview("Launch a blog", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plan preview tasks are required");

        verify(executionLogService).recordFailure(null, "plan-preview.create", "Launch a blog", "plan preview tasks are required");
    }

    private static PlanPreviewView preview() {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new PlanPreviewView(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Launch a blog",
                PlanPreviewStatus.PENDING,
                List.of(new PlanPreviewTaskView(
                        UUID.randomUUID(),
                        "Choose platform",
                        null,
                        null,
                        TodoPriority.HIGH,
                        "work",
                        30,
                        null,
                        null,
                        0,
                        now
                )),
                now,
                now
        );
    }
}
