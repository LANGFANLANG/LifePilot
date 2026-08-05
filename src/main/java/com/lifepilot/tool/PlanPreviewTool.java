package com.lifepilot.tool;

import com.lifepilot.agent.PlanPreviewActionContext;
import com.lifepilot.service.ExecutionLogService;
import com.lifepilot.service.PlanPreviewService;
import com.lifepilot.service.dto.CreatePlanPreviewCommand;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.PlanPreviewView;
import com.lifepilot.util.TimeParser;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 供 AI 创建和读取计划草案的工具。
 */
@Component
public class PlanPreviewTool {

    private final PlanPreviewService planPreviewService;
    private final ExecutionLogService executionLogService;
    private final PlanPreviewActionContext planPreviewActionContext;

    public PlanPreviewTool(
            PlanPreviewService planPreviewService,
            ExecutionLogService executionLogService,
            PlanPreviewActionContext planPreviewActionContext
    ) {
        this.planPreviewService = planPreviewService;
        this.executionLogService = executionLogService;
        this.planPreviewActionContext = planPreviewActionContext;
    }

    @Tool(description = "Create a plan preview with draft todo tasks. Use this for multi-step goals that need user confirmation before creating real todos.")
    public ToolResult createPlanPreview(
            @ToolParam(description = "The user's goal") String goal,
            @ToolParam(description = "Draft tasks for the goal") List<PlanPreviewTaskInput> tasks
    ) {
        try {
            PlanPreviewView preview = planPreviewService.create(new CreatePlanPreviewCommand(
                    null,
                    goal,
                    tasks.stream().map(this::toCreateTodoCommand).toList()
            ));
            planPreviewActionContext.recordPlanPreview(preview.id(), "确认计划：" + preview.goal());
            executionLogService.recordSuccess(null, "plan-preview.create", goal, preview.toString());
            return ToolResult.success("plan preview created", preview);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "plan-preview.create", goal, ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "Get a plan preview by ID.")
    public ToolResult getPlanPreview(@ToolParam(description = "Plan preview ID") UUID id) {
        String input = id.toString();
        try {
            PlanPreviewView preview = planPreviewService.get(id);
            executionLogService.recordSuccess(null, "plan-preview.get", input, preview.toString());
            return ToolResult.success("plan preview found", preview);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "plan-preview.get", input, ex.getMessage());
            throw ex;
        }
    }

    private CreateTodoCommand toCreateTodoCommand(PlanPreviewTaskInput input) {
        return new CreateTodoCommand(
                input.title(),
                input.description(),
                TimeParser.parseOffsetDateTime(input.dueAt()),
                input.priority(),
                input.category(),
                input.estimatedMinutes(),
                TimeParser.parseOffsetDateTime(input.plannedStartAt()),
                TimeParser.parseOffsetDateTime(input.reminderAt()),
                null,
                "ai-preview"
        );
    }
}
