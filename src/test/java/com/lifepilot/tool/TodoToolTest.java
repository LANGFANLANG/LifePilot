package com.lifepilot.tool;

import com.lifepilot.domain.TodoStatus;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.service.ExecutionLogService;
import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
import com.lifepilot.service.dto.UpdateTodoCommand;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoToolTest {

    @Mock
    private TodoService todoService;

    @Mock
    private ExecutionLogService executionLogService;

    @InjectMocks
    private TodoTool todoTool;

    @Test
    void createsTodoThroughService() {
        TodoView todo = todo("Buy milk", "2 bottles", TodoStatus.PENDING);
        when(todoService.create(new CreateTodoCommand(
                "Buy milk",
                "2 bottles",
                null,
                TodoPriority.HIGH,
                "life",
                10,
                null,
                null,
                null,
                "ai"
        )))
                .thenReturn(todo);

        ToolResult result = todoTool.createTodo("Buy milk", "2 bottles", null, TodoPriority.HIGH, "life", 10, null, null);

        ArgumentCaptor<CreateTodoCommand> captor = ArgumentCaptor.forClass(CreateTodoCommand.class);
        verify(todoService).create(captor.capture());
        verify(executionLogService).recordSuccess(null, "todo.create", "Buy milk", todo.toString());
        assertThat(captor.getValue().title()).isEqualTo("Buy milk");
        assertThat(captor.getValue().priority()).isEqualTo(TodoPriority.HIGH);
        assertThat(captor.getValue().source()).isEqualTo("ai");
        assertThat(result).isEqualTo(ToolResult.success("todo created", todo));
    }

    @Test
    void listsTodosThroughService() {
        List<TodoView> todos = List.of(todo("Buy milk", "2 bottles", TodoStatus.PENDING));
        when(todoService.list()).thenReturn(todos);

        ToolResult result = todoTool.listTodos();

        verify(todoService).list();
        verify(executionLogService).recordSuccess(null, "todo.list", "", todos.toString());
        assertThat(result).isEqualTo(ToolResult.success("todos listed", todos));
    }

    @Test
    void completesTodoThroughService() {
        UUID todoId = UUID.randomUUID();
        TodoView completed = todo("Buy milk", "2 bottles", TodoStatus.COMPLETED);
        when(todoService.complete(todoId)).thenReturn(completed);

        ToolResult result = todoTool.completeTodo(todoId);

        verify(todoService).complete(todoId);
        verify(executionLogService).recordSuccess(null, "todo.complete", todoId.toString(), completed.toString());
        assertThat(result).isEqualTo(ToolResult.success("todo completed", completed));
    }

    @Test
    void recordsFailedTodoExecutionAndRethrows() {
        UUID todoId = UUID.randomUUID();
        when(todoService.complete(todoId)).thenThrow(new IllegalArgumentException("todo not found"));

        assertThatThrownBy(() -> todoTool.completeTodo(todoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("todo not found");

        verify(executionLogService).recordFailure(null, "todo.complete", todoId.toString(), "todo not found");
    }

    @Test
    void updatesTodoThroughService() {
        UUID todoId = UUID.randomUUID();
        TodoView updated = todo("Buy coffee", "beans", TodoStatus.PENDING);
        when(todoService.update(todoId, new UpdateTodoCommand(
                "Buy coffee",
                "beans",
                null,
                TodoPriority.HIGH,
                "life",
                15,
                null,
                null,
                null,
                "ai"
        ))).thenReturn(updated);

        ToolResult result = todoTool.updateTodo(todoId, "Buy coffee", "beans", null, TodoPriority.HIGH, "life", 15, null, null);

        verify(todoService).update(todoId, new UpdateTodoCommand(
                "Buy coffee",
                "beans",
                null,
                TodoPriority.HIGH,
                "life",
                15,
                null,
                null,
                null,
                "ai"
        ));
        verify(executionLogService).recordSuccess(null, "todo.update", todoId + ":Buy coffee", updated.toString());
        assertThat(result).isEqualTo(ToolResult.success("todo updated", updated));
    }

    @Test
    void deletesTodoThroughService() {
        UUID todoId = UUID.randomUUID();

        ToolResult result = todoTool.deleteTodo(todoId);

        verify(todoService).delete(todoId);
        verify(executionLogService).recordSuccess(null, "todo.delete", todoId.toString(), "deleted");
        assertThat(result).isEqualTo(ToolResult.success("todo deleted", todoId));
    }

    private static TodoView todo(String title, String description, TodoStatus status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new TodoView(
                UUID.randomUUID(),
                title,
                description,
                status,
                null,
                TodoPriority.MEDIUM,
                "life",
                10,
                null,
                null,
                null,
                null,
                "manual",
                0,
                now,
                now
        );
    }
}
