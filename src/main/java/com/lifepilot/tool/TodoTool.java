package com.lifepilot.tool;

import com.lifepilot.domain.TodoPriority;
import com.lifepilot.service.ExecutionLogService;
import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
import com.lifepilot.service.dto.UpdateTodoCommand;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class TodoTool {

    private final TodoService todoService;
    private final ExecutionLogService executionLogService;

    public TodoTool(TodoService todoService, ExecutionLogService executionLogService) {
        this.todoService = todoService;
        this.executionLogService = executionLogService;
    }

    @Tool(description = "Create a new todo task with optional planning metadata.")
    public ToolResult createTodo(
            @ToolParam(description = "Todo title") String title,
            @ToolParam(required = false, description = "Todo details") String description,
            @ToolParam(required = false, description = "Due date/time in ISO-8601 format") OffsetDateTime dueAt,
            @ToolParam(required = false, description = "Priority: LOW, MEDIUM, or HIGH") TodoPriority priority,
            @ToolParam(required = false, description = "Category, such as work, learning, or life") String category,
            @ToolParam(required = false, description = "Estimated duration in minutes") Integer estimatedMinutes,
            @ToolParam(required = false, description = "Planned start date/time in ISO-8601 format") OffsetDateTime plannedStartAt,
            @ToolParam(required = false, description = "Reminder date/time in ISO-8601 format") OffsetDateTime reminderAt
    ) {
        try {
            TodoView todo = todoService.create(new CreateTodoCommand(
                    title,
                    description,
                    dueAt,
                    priority,
                    category,
                    estimatedMinutes,
                    plannedStartAt,
                    reminderAt,
                    null,
                    "ai"
            ));
            executionLogService.recordSuccess(null, "todo.create", title, todo.toString());
            return ToolResult.success("todo created", todo);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "todo.create", title, ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "List all todo tasks.")
    public ToolResult listTodos() {
        try {
            List<TodoView> todos = todoService.list();
            executionLogService.recordSuccess(null, "todo.list", "", todos.toString());
            return ToolResult.success("todos listed", todos);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "todo.list", "", ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "Mark a todo task as completed.")
    public ToolResult completeTodo(@ToolParam(description = "Todo ID") UUID id) {
        String input = id.toString();
        try {
            TodoView todo = todoService.complete(id);
            executionLogService.recordSuccess(null, "todo.complete", input, todo.toString());
            return ToolResult.success("todo completed", todo);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "todo.complete", input, ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "Update a todo task and its planning metadata.")
    public ToolResult updateTodo(
            @ToolParam(description = "Todo ID") UUID id,
            @ToolParam(description = "Todo title") String title,
            @ToolParam(required = false, description = "Todo details") String description,
            @ToolParam(required = false, description = "Due date/time in ISO-8601 format") OffsetDateTime dueAt,
            @ToolParam(required = false, description = "Priority: LOW, MEDIUM, or HIGH") TodoPriority priority,
            @ToolParam(required = false, description = "Category") String category,
            @ToolParam(required = false, description = "Estimated duration in minutes") Integer estimatedMinutes,
            @ToolParam(required = false, description = "Planned start date/time in ISO-8601 format") OffsetDateTime plannedStartAt,
            @ToolParam(required = false, description = "Reminder date/time in ISO-8601 format") OffsetDateTime reminderAt
    ) {
        String input = id + ":" + title;
        try {
            TodoView todo = todoService.update(id, new UpdateTodoCommand(
                    title,
                    description,
                    dueAt,
                    priority,
                    category,
                    estimatedMinutes,
                    plannedStartAt,
                    reminderAt,
                    null,
                    "ai"
            ));
            executionLogService.recordSuccess(null, "todo.update", input, todo.toString());
            return ToolResult.success("todo updated", todo);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "todo.update", input, ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "Delete a todo task.")
    public ToolResult deleteTodo(@ToolParam(description = "Todo ID") UUID id) {
        String input = id.toString();
        try {
            todoService.delete(id);
            executionLogService.recordSuccess(null, "todo.delete", input, "deleted");
            return ToolResult.success("todo deleted", id);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "todo.delete", input, ex.getMessage());
            throw ex;
        }
    }
}
