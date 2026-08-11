package com.lifepilot.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.agent.AgentService;
import com.lifepilot.agent.dto.AgentRequest;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.controller.dto.ChatRequest;
import com.lifepilot.domain.ChatRole;
import com.lifepilot.memory.ChatMemoryService;
import com.lifepilot.memory.dto.ConversationView;
import com.lifepilot.memory.dto.MessageView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentService agentService;

    @MockBean
    private ChatMemoryService chatMemoryService;

    @Test
    void chatsWithAgentForCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(agentService.chat(any())).thenReturn(new AgentResponse(conversationId, "Task created"));

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsString).thenReturn(userId.toString());

            mockMvc.perform(post("/api/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ChatRequest(conversationId, "Create a task")
                            )))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.conversationId").value(conversationId.toString()))
                    .andExpect(jsonPath("$.data.content").value("Task created"));
        }

        ArgumentCaptor<AgentRequest> captor = ArgumentCaptor.forClass(AgentRequest.class);
        verify(agentService).chat(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().conversationId()).isEqualTo(conversationId);
        assertThat(captor.getValue().message()).isEqualTo("Create a task");
    }

    @Test
    void listsCurrentUsersConversations() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(chatMemoryService.listConversations(userId)).thenReturn(List.of(
                new ConversationView(
                        conversationId,
                        "Project planning",
                        OffsetDateTime.parse("2026-08-11T10:00:00+08:00"),
                        OffsetDateTime.parse("2026-08-11T10:05:00+08:00")
                )
        ));

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsString).thenReturn(userId.toString());

            mockMvc.perform(get("/api/chat/conversations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].id").value(conversationId.toString()))
                    .andExpect(jsonPath("$.data[0].title").value("Project planning"));
        }
    }

    @Test
    void loadsCurrentUsersConversationMessages() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        when(chatMemoryService.loadMessages(userId, conversationId)).thenReturn(List.of(
                new MessageView(
                        messageId,
                        conversationId,
                        ChatRole.USER,
                        "Hello",
                        OffsetDateTime.parse("2026-08-11T10:00:00+08:00")
                )
        ));

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsString).thenReturn(userId.toString());

            mockMvc.perform(get("/api/chat/conversations/{conversationId}/messages", conversationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].id").value(messageId.toString()))
                    .andExpect(jsonPath("$.data[0].content").value("Hello"));
        }
    }
}
