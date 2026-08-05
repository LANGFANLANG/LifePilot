package com.lifepilot.agent;

import com.lifepilot.agent.dto.AgentRequest;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.domain.ChatRole;
import com.lifepilot.memory.ChatMemoryService;
import com.lifepilot.memory.dto.ConversationView;
import com.lifepilot.memory.dto.MessageView;
import com.lifepilot.service.ExecutionLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AgentServiceTest {

    @Mock
    private ChatMemoryService chatMemoryService;

    @Mock
    private AiClient aiClient;

    @Mock
    private ExecutionLogService executionLogService;

    @Mock
    private PlanPreviewActionContext planPreviewActionContext;

    @InjectMocks
    private AgentService agentService;

    @Test
    void savesMessagesAndReturnsConversationResponse() {
        UUID conversationId = UUID.randomUUID();
        MessageView userMessage = messageView(conversationId, ChatRole.USER, "Create a task");
        MessageView assistantMessage = messageView(conversationId, ChatRole.ASSISTANT, "Task created");
        when(chatMemoryService.appendMessage(conversationId, ChatRole.USER, "Create a task"))
                .thenReturn(userMessage);
        when(chatMemoryService.loadRecentMessages(conversationId)).thenReturn(List.of(userMessage));
        when(aiClient.chat(List.of(userMessage))).thenReturn("Task created");
        when(planPreviewActionContext.currentActions()).thenReturn(List.of());
        when(chatMemoryService.appendMessage(conversationId, ChatRole.ASSISTANT, "Task created"))
                .thenReturn(assistantMessage);

        AgentResponse response = agentService.chat(new AgentRequest(conversationId, "Create a task"));

        verify(aiClient).chat(List.of(userMessage));
        verify(chatMemoryService).appendMessage(conversationId, ChatRole.USER, "Create a task");
        verify(chatMemoryService).appendMessage(conversationId, ChatRole.ASSISTANT, "Task created");
        verify(executionLogService).recordSuccess(conversationId, "agent.chat", "Create a task", "Task created");
        assertThat(response.conversationId()).isEqualTo(conversationId);
        assertThat(response.content()).isEqualTo("Task created");
        assertThat(response.actions()).isEmpty();
    }

    @Test
    void createsConversationWhenRequestHasNoConversationId() {
        UUID conversationId = UUID.randomUUID();
        ConversationView conversation = new ConversationView(
                conversationId,
                "Create a task",
                OffsetDateTime.parse("2026-07-10T10:00:00+08:00"),
                OffsetDateTime.parse("2026-07-10T10:00:00+08:00")
        );
        MessageView userMessage = messageView(conversationId, ChatRole.USER, "Create a task");
        when(chatMemoryService.createConversation("Create a task")).thenReturn(conversation);
        when(chatMemoryService.appendMessage(conversationId, ChatRole.USER, "Create a task"))
                .thenReturn(userMessage);
        when(chatMemoryService.loadRecentMessages(conversationId)).thenReturn(List.of(userMessage));
        when(aiClient.chat(List.of(userMessage))).thenReturn("Task created");
        when(planPreviewActionContext.currentActions()).thenReturn(List.of());
        when(chatMemoryService.appendMessage(conversationId, ChatRole.ASSISTANT, "Task created"))
                .thenReturn(messageView(conversationId, ChatRole.ASSISTANT, "Task created"));

        AgentResponse response = agentService.chat(new AgentRequest(null, "Create a task"));

        verify(chatMemoryService).createConversation("Create a task");
        assertThat(response.conversationId()).isEqualTo(conversationId);
    }

    @Test
    void recordsFailedAgentExecutionAndRethrows() {
        UUID conversationId = UUID.randomUUID();
        MessageView userMessage = messageView(conversationId, ChatRole.USER, "Create a task");
        when(chatMemoryService.appendMessage(conversationId, ChatRole.USER, "Create a task"))
                .thenReturn(userMessage);
        when(chatMemoryService.loadRecentMessages(conversationId)).thenReturn(List.of(userMessage));
        when(aiClient.chat(List.of(userMessage))).thenThrow(new IllegalStateException("model unavailable"));

        assertThatThrownBy(() -> agentService.chat(new AgentRequest(conversationId, "Create a task")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("model unavailable");

        verify(executionLogService).recordFailure(conversationId, "agent.chat", "Create a task", "model unavailable");
    }

    private static MessageView messageView(UUID conversationId, ChatRole role, String content) {
        return new MessageView(
                UUID.randomUUID(),
                conversationId,
                role,
                content,
                OffsetDateTime.parse("2026-07-10T10:00:00+08:00")
        );
    }
}
