package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.CreatePlanPreviewRequest;
import com.lifepilot.controller.dto.PlanPreviewTaskRequest;
import com.lifepilot.service.PlanPreviewService;
import com.lifepilot.service.dto.CreatePlanPreviewCommand;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.PlanPreviewView;
import com.lifepilot.service.dto.TodoView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 提供 AI 计划草案的创建、查看、确认和拒绝接口。
 */
@RestController
@RequestMapping("/api/plan-previews")
public class PlanPreviewController {

    private final PlanPreviewService planPreviewService;

    public PlanPreviewController(PlanPreviewService planPreviewService) {
        this.planPreviewService = planPreviewService;
    }

    @PostMapping
    public Result<PlanPreviewView> create(@Valid @RequestBody CreatePlanPreviewRequest request) {
        return Result.success(planPreviewService.create(new CreatePlanPreviewCommand(
                request.conversationId(),
                request.goal(),
                request.tasks().stream().map(this::toCreateTodoCommand).toList()
        )));
    }

    @GetMapping("/{id}")
    public Result<PlanPreviewView> get(@PathVariable UUID id) {
        return Result.success(planPreviewService.get(id));
    }

    @PostMapping("/{id}/confirm")
    public Result<List<TodoView>> confirm(@PathVariable UUID id) {
        return Result.success(planPreviewService.confirm(id));
    }

    @PostMapping("/{id}/reject")
    public Result<PlanPreviewView> reject(@PathVariable UUID id) {
        return Result.success(planPreviewService.reject(id));
    }

    private CreateTodoCommand toCreateTodoCommand(PlanPreviewTaskRequest request) {
        return new CreateTodoCommand(
                request.title(),
                request.description(),
                request.dueAt(),
                request.priority(),
                request.category(),
                request.estimatedMinutes(),
                request.plannedStartAt(),
                request.reminderAt(),
                null,
                "ai-preview"
        );
    }
}
