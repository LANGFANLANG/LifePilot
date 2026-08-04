package com.lifepilot.service;

import com.lifepilot.domain.Todo;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
import com.lifepilot.service.dto.UpdateTodoCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 协调待办事项用例，并将领域实体转换为服务层 DTO。
 */
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    /**
     * 创建待办事项应用服务。
     *
     * @param todoRepository 待办事项持久化访问入口
     */
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * 根据创建命令新增待处理待办事项。
     *
     * @param command 待办创建数据
     * @return 已创建的待办视图
     */
    @Transactional
    public TodoView create(CreateTodoCommand command) {
        Todo todo = Todo.create(
                command.title(),
                command.description(),
                command.dueAt(),
                command.priority(),
                command.category(),
                command.estimatedMinutes(),
                command.plannedStartAt(),
                command.reminderAt(),
                command.parentTodoId(),
                command.source()
        );
        return TodoView.from(todoRepository.save(todo));
    }

    /**
     * 按创建时间倒序获取待办事项列表。
     *
     * @return 已排序的待办视图列表
     */
    @Transactional(readOnly = true)
    public List<TodoView> list() {
        return todoRepository.findAll().stream()
                .sorted(Comparator.comparing(Todo::getCreatedAt).reversed())
                .map(TodoView::from)
                .toList();
    }

    /**
     * 将指定待办事项标记为已完成。
     *
     * @param id 待办事项标识
     * @return 已完成的待办视图
     * @throws IllegalArgumentException 待办事项不存在时抛出
     */
    @Transactional
    public TodoView complete(UUID id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("todo not found"));
        todo.complete();
        return TodoView.from(todoRepository.save(todo));
    }

    @Transactional
    public TodoView update(UUID id, UpdateTodoCommand command) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("todo not found"));
        todo.update(
                command.title(),
                command.description(),
                command.dueAt(),
                command.priority(),
                command.category(),
                command.estimatedMinutes(),
                command.plannedStartAt(),
                command.reminderAt(),
                command.parentTodoId(),
                command.source()
        );
        return TodoView.from(todoRepository.save(todo));
    }

    @Transactional
    public void delete(UUID id) {
        if (!todoRepository.existsById(id)) {
            throw new IllegalArgumentException("todo not found");
        }
        todoRepository.deleteById(id);
    }
}
