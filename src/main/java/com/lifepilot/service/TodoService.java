package com.lifepilot.service;

import com.lifepilot.domain.Todo;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional
    public TodoView create(CreateTodoCommand command) {
        Todo todo = Todo.create(command.title(), command.description(), command.dueAt());
        return TodoView.from(todoRepository.save(todo));
    }

    @Transactional(readOnly = true)
    public List<TodoView> list() {
        return todoRepository.findAll().stream()
                .sorted(Comparator.comparing(Todo::getCreatedAt).reversed())
                .map(TodoView::from)
                .toList();
    }

    @Transactional
    public TodoView complete(UUID id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("todo not found"));
        todo.complete();
        return TodoView.from(todoRepository.save(todo));
    }
}
