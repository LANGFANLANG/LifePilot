package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationTest {

    @Test
    void createsConversationAndAddsMessages() {
        Conversation conversation = Conversation.create("Project planning");

        ChatMessage userMessage = conversation.addMessage(ChatRole.USER, "Create a task");
        ChatMessage assistantMessage = conversation.addMessage(ChatRole.ASSISTANT, "Task created");

        assertThat(conversation.getId()).isNotNull();
        assertThat(conversation.getTitle()).isEqualTo("Project planning");
        assertThat(userMessage.getConversationId()).isEqualTo(conversation.getId());
        assertThat(userMessage.getRole()).isEqualTo(ChatRole.USER);
        assertThat(assistantMessage.getRole()).isEqualTo(ChatRole.ASSISTANT);
    }
}
