package com.lifepilot.service;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoPriority;
import com.lifepilot.domain.TodoStatus;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.service.dto.CreateTodoCommand;
import com.lifepilot.service.dto.TodoView;
import com.lifepilot.service.dto.UpdateTodoCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    void createsTodo() {
        when(todoRepository.save(any(Todo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TodoView view = todoService.create(new CreateTodoCommand("Buy milk", "2 bottles", null));

        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(todoRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Buy milk");
        assertThat(view.title()).isEqualTo("Buy milk");
        assertThat(view.status()).isEqualTo(TodoStatus.PENDING);
        assertThat(view.priority()).isEqualTo(TodoPriority.MEDIUM);
    }

    @Test
    void createsTodoWithPlanningMetadata() {
        when(todoRepository.save(any(Todo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TodoView view = todoService.create(new CreateTodoCommand(
                "Write plan",
                "Draft the implementation plan",
                null,
                TodoPriority.HIGH,
                "work",
                45,
                null,
                null,
                null,
                "manual"
        ));

        assertThat(view.priority()).isEqualTo(TodoPriority.HIGH);
        assertThat(view.category()).isEqualTo("work");
        assertThat(view.estimatedMinutes()).isEqualTo(45);
    }

    @Test
    void listsTodos() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);
        when(todoRepository.findAll()).thenReturn(List.of(todo));

        List<TodoView> todos = todoService.list();

        assertThat(todos).hasSize(1);
        assertThat(todos.getFirst().id()).isEqualTo(todo.getId());
    }

    @Test
    void completesTodo() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);
        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TodoView completed = todoService.complete(todo.getId());

        assertThat(completed.status()).isEqualTo(TodoStatus.COMPLETED);
    }

    @Test
    void throwsWhenCompletingMissingTodo() {
        UUID missingId = UUID.randomUUID();
        when(todoRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.complete(missingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("todo not found");
    }

    @Test
    void updatesTodo() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);
        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TodoView updated = todoService.update(todo.getId(), new UpdateTodoCommand(
                "Buy coffee",
                "beans",
                null,
                TodoPriority.HIGH,
                "life",
                15,
                null,
                null,
                null,
                "manual"
        ));

        assertThat(updated.title()).isEqualTo("Buy coffee");
        assertThat(updated.priority()).isEqualTo(TodoPriority.HIGH);
        assertThat(updated.category()).isEqualTo("life");
        assertThat(updated.estimatedMinutes()).isEqualTo(15);
    }

    @Test
    void deletesTodo() {
        UUID id = UUID.randomUUID();
        when(todoRepository.existsById(id)).thenReturn(true);

        todoService.delete(id);

        verify(todoRepository).deleteById(id);
    }

    @Test
    void throwsWhenDeletingMissingTodo() {
        UUID missingId = UUID.randomUUID();
        when(todoRepository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> todoService.delete(missingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("todo not found");
    }
}
