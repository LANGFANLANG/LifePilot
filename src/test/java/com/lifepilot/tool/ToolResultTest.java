package com.lifepilot.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolResultTest {

    @Test
    void createsSuccessfulResult() {
        ToolResult result = ToolResult.success("todo created", "todo-data");

        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("todo created");
        assertThat(result.data()).isEqualTo("todo-data");
    }

    @Test
    void createsFailedResultWithoutData() {
        ToolResult result = ToolResult.failure("todo not found");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("todo not found");
        assertThat(result.data()).isNull();
    }
}
