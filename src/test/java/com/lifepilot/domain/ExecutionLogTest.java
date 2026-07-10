package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionLogTest {

    @Test
    void createsSuccessExecutionLog() {
        UUID conversationId = UUID.randomUUID();

        ExecutionLog log = ExecutionLog.success(conversationId, "agent.chat", "hello", "hi");

        assertThat(log.getId()).isNotNull();
        assertThat(log.getConversationId()).isEqualTo(conversationId);
        assertThat(log.getActionType()).isEqualTo("agent.chat");
        assertThat(log.getInput()).isEqualTo("hello");
        assertThat(log.getOutput()).isEqualTo("hi");
        assertThat(log.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void createsFailedExecutionLog() {
        ExecutionLog log = ExecutionLog.failure(null, "todo.complete", "missing-id", "todo not found");

        assertThat(log.getId()).isNotNull();
        assertThat(log.getConversationId()).isNull();
        assertThat(log.getActionType()).isEqualTo("todo.complete");
        assertThat(log.getInput()).isEqualTo("missing-id");
        assertThat(log.getOutput()).isNull();
        assertThat(log.getStatus()).isEqualTo(ExecutionStatus.FAILURE);
        assertThat(log.getErrorMessage()).isEqualTo("todo not found");
        assertThat(log.getCreatedAt()).isNotNull();
    }
}
