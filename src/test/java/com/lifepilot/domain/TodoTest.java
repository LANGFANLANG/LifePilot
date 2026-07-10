package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TodoTest {

    @Test
    void createPendingTodo() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);

        assertThat(todo.getTitle()).isEqualTo("Buy milk");
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.PENDING);
        assertThat(todo.getId()).isNotNull();
    }
}
