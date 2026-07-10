package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.CreateTodoRequest;
import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.CreateTodoCommand;
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

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public Result<TodoView> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoView todo = todoService.create(new CreateTodoCommand(
                request.title(),
                request.description(),
                request.dueAt()
        ));
        return Result.success(todo);
    }

    @GetMapping
    public Result<List<TodoView>> list() {
        return Result.success(todoService.list());
    }

    @PostMapping("/{id}/complete")
    public Result<TodoView> complete(@PathVariable UUID id) {
        return Result.success(todoService.complete(id));
    }
}
