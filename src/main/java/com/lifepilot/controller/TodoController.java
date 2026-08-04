package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.CreateTodoRequest;
import com.lifepilot.controller.dto.UpdateTodoRequest;
import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
import com.lifepilot.service.dto.UpdateTodoCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 提供待办事项创建、查询和完成操作的 HTTP 接口。
 */
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    /**
     * 创建待办事项接口控制器。
     *
     * @param todoService 待办事项应用服务
     */
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 创建待办事项。
     *
     * @param request 已校验的待办创建请求
     * @return 包含已创建待办事项的成功响应
     */
    @PostMapping
    public Result<TodoView> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoView todo = todoService.create(new CreateTodoCommand(
                request.title(),
                request.description(),
                request.dueAt(),
                request.priority(),
                request.category(),
                request.estimatedMinutes(),
                request.plannedStartAt(),
                request.reminderAt(),
                request.parentTodoId(),
                request.source()
        ));
        return Result.success(todo);
    }

    /**
     * 查询全部待办事项。
     *
     * @return 包含排序后待办事项的成功响应
     */
    @GetMapping
    public Result<List<TodoView>> list() {
        return Result.success(todoService.list());
    }

    /**
     * 完成指定待办事项。
     *
     * @param id 待办事项标识
     * @return 包含已完成待办事项的成功响应
     */
    @PostMapping("/{id}/complete")
    public Result<TodoView> complete(@PathVariable UUID id) {
        return Result.success(todoService.complete(id));
    }

    @PutMapping("/{id}")
    public Result<TodoView> update(@PathVariable UUID id, @Valid @RequestBody UpdateTodoRequest request) {
        TodoView todo = todoService.update(id, new UpdateTodoCommand(
                request.title(),
                request.description(),
                request.dueAt(),
                request.priority(),
                request.category(),
                request.estimatedMinutes(),
                request.plannedStartAt(),
                request.reminderAt(),
                request.parentTodoId(),
                request.source()
        ));
        return Result.success(todo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        todoService.delete(id);
        return Result.success(null);
    }
}
