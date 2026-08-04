package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TodoTest {

    @Test
    void createPendingTodo() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);

        assertThat(todo.getTitle()).isEqualTo("Buy milk");
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.PENDING);
        assertThat(todo.getPriority()).isEqualTo(TodoPriority.MEDIUM);
        assertThat(todo.getSource()).isEqualTo("manual");
        assertThat(todo.getPostponementCount()).isZero();
        assertThat(todo.getId()).isNotNull();
    }

    @Test
    void completesTodoWithCompletedAt() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);

        todo.complete();

        assertThat(todo.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(todo.getCompletedAt()).isNotNull();
    }
}
