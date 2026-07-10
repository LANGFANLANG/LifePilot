package com.lifepilot.agent;

import com.lifepilot.domain.ChatRole;
import com.lifepilot.memory.dto.MessageView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAiClientTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Test
    void convertsMemoryRolesAndReturnsAiContent() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(anyList())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Task created");
        SpringAiClient aiClient = new SpringAiClient(chatClient);

        String response = aiClient.chat(List.of(
                message(ChatRole.SYSTEM, "You are helpful"),
                message(ChatRole.USER, "Create a task"),
                message(ChatRole.ASSISTANT, "What task?"),
                message(ChatRole.TOOL, "Todo created")
        ));

        ArgumentCaptor<List<Message>> messagesCaptor = messageListCaptor();
        verify(requestSpec).messages(messagesCaptor.capture());
        assertThat(messagesCaptor.getValue())
                .hasExactlyElementsOfTypes(SystemMessage.class, UserMessage.class, AssistantMessage.class,
                        ToolResponseMessage.class);
        assertThat(response).isEqualTo("Task created");
    }

    private static MessageView message(ChatRole role, String content) {
        return new MessageView(
                UUID.randomUUID(),
                UUID.randomUUID(),
                role,
                content,
                OffsetDateTime.parse("2026-07-10T10:00:00+08:00")
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ArgumentCaptor<List<Message>> messageListCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
    }
}
