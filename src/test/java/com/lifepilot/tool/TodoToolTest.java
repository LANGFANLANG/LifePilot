package com.lifepilot.tool;

import com.lifepilot.domain.TodoStatus;
import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoToolTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoTool todoTool;

    @Test
    void createsTodoThroughService() {
        TodoView todo = todo("Buy milk", "2 bottles", TodoStatus.PENDING);
        when(todoService.create(new CreateTodoCommand("Buy milk", "2 bottles", null)))
                .thenReturn(todo);

        ToolResult result = todoTool.createTodo("Buy milk", "2 bottles", null);

        ArgumentCaptor<CreateTodoCommand> captor = ArgumentCaptor.forClass(CreateTodoCommand.class);
        verify(todoService).create(captor.capture());
        assertThat(captor.getValue().title()).isEqualTo("Buy milk");
        assertThat(result).isEqualTo(ToolResult.success("todo created", todo));
    }

    @Test
    void listsTodosThroughService() {
        List<TodoView> todos = List.of(todo("Buy milk", "2 bottles", TodoStatus.PENDING));
        when(todoService.list()).thenReturn(todos);

        ToolResult result = todoTool.listTodos();

        verify(todoService).list();
        assertThat(result).isEqualTo(ToolResult.success("todos listed", todos));
    }

    @Test
    void completesTodoThroughService() {
        UUID todoId = UUID.randomUUID();
        TodoView completed = todo("Buy milk", "2 bottles", TodoStatus.COMPLETED);
        when(todoService.complete(todoId)).thenReturn(completed);

        ToolResult result = todoTool.completeTodo(todoId);

        verify(todoService).complete(todoId);
        assertThat(result).isEqualTo(ToolResult.success("todo completed", completed));
    }

    private static TodoView todo(String title, String description, TodoStatus status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-10T10:00:00+08:00");
        return new TodoView(UUID.randomUUID(), title, description, status, null, now, now);
    }
}
