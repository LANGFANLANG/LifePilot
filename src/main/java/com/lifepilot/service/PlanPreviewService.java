package com.lifepilot.service;

import com.lifepilot.domain.PlanPreview;
import com.lifepilot.domain.PlanPreviewTask;
import com.lifepilot.repository.PlanPreviewRepository;
import com.lifepilot.repository.PlanPreviewTaskRepository;
import com.lifepilot.service.dto.CreatePlanPreviewCommand;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.PlanPreviewView;
import com.lifepilot.service.dto.TodoView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 管理 AI 计划草案以及确认落库流程。
 */
@Service
public class PlanPreviewService {

    private final PlanPreviewRepository planPreviewRepository;
    private final PlanPreviewTaskRepository planPreviewTaskRepository;
    private final TodoService todoService;

    public PlanPreviewService(
            PlanPreviewRepository planPreviewRepository,
            PlanPreviewTaskRepository planPreviewTaskRepository,
            TodoService todoService
    ) {
        this.planPreviewRepository = planPreviewRepository;
        this.planPreviewTaskRepository = planPreviewTaskRepository;
        this.todoService = todoService;
    }

    @Transactional
    public PlanPreviewView create(CreatePlanPreviewCommand command) {
        List<CreateTodoCommand> taskCommands = command.tasks() == null ? List.of() : command.tasks();
        List<PlanPreviewTask> tasks = IntStream.range(0, taskCommands.size())
                .mapToObj(index -> toPreviewTask(taskCommands.get(index), index))
                .toList();
        PlanPreview preview = PlanPreview.create(command.conversationId(), command.goal(), tasks);
        planPreviewRepository.save(preview);
        saveTasks(preview);
        return PlanPreviewView.from(preview);
    }

    @Transactional(readOnly = true)
    public PlanPreviewView get(UUID id) {
        return PlanPreviewView.from(find(id));
    }

    @Transactional
    public List<TodoView> confirm(UUID id) {
        PlanPreview preview = find(id);
        List<TodoView> todos = preview.getTasks().stream()
                .map(this::toCreateTodoCommand)
                .map(todoService::create)
                .toList();
        preview.confirm();
        planPreviewRepository.save(preview);
        return todos;
    }

    @Transactional
    public PlanPreviewView reject(UUID id) {
        PlanPreview preview = find(id);
        preview.reject();
        return PlanPreviewView.from(planPreviewRepository.save(preview));
    }

    private PlanPreview find(UUID id) {
        PlanPreview preview = planPreviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("plan preview not found"));
        preview.replaceTasks(planPreviewTaskRepository.findByPlanPreviewIdOrderBySortOrderAsc(id));
        return preview;
    }

    private void saveTasks(PlanPreview preview) {
        planPreviewTaskRepository.deleteByPlanPreviewId(preview.getId());
        preview.getTasks().forEach(task -> {
            task.assignToPreview(preview.getId());
            planPreviewTaskRepository.save(task);
        });
    }

    private PlanPreviewTask toPreviewTask(CreateTodoCommand command, int sortOrder) {
        return PlanPreviewTask.create(
                command.title(),
                command.description(),
                command.dueAt(),
                command.priority(),
                command.category(),
                command.estimatedMinutes(),
                command.plannedStartAt(),
                command.reminderAt(),
                sortOrder
        );
    }

    private CreateTodoCommand toCreateTodoCommand(PlanPreviewTask task) {
        return new CreateTodoCommand(
                task.getTitle(),
                task.getDescription(),
                task.getDueAt(),
                task.getPriority(),
                task.getCategory(),
                task.getEstimatedMinutes(),
                task.getPlannedStartAt(),
                task.getReminderAt(),
                null,
                "ai-plan"
        );
    }
}
