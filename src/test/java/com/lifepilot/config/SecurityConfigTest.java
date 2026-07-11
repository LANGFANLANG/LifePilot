package com.lifepilot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.agent.AgentService;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.controller.ChatController;
import com.lifepilot.controller.dto.ChatRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityConfigTest {

    @Nested
    @WebMvcTest(ChatController.class)
    @AutoConfigureMockMvc
    @Import(SecurityConfig.class)
    @ImportAutoConfiguration({
            EndpointAutoConfiguration.class,
            ManagementContextAutoConfiguration.class,
            WebEndpointAutoConfiguration.class,
            HealthEndpointAutoConfiguration.class
    })
    @TestPropertySource(properties = "lifepilot.security.auth-enabled=false")
    class PasswordlessDevSecurity {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AgentService agentService;

        @Test
        void exposesHealthEndpointWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        void allowsChatWithoutAuthenticationWhenAuthIsDisabled() throws Exception {
            UUID conversationId = UUID.randomUUID();
            when(agentService.chat(any())).thenReturn(new AgentResponse(conversationId, "Task created"));

            mockMvc.perform(post("/api/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ChatRequest(conversationId, "Create a task")
                            )))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @WebMvcTest(ChatController.class)
    @AutoConfigureMockMvc
    @Import(SecurityConfig.class)
    @ImportAutoConfiguration({
            EndpointAutoConfiguration.class,
            ManagementContextAutoConfiguration.class,
            WebEndpointAutoConfiguration.class,
            HealthEndpointAutoConfiguration.class
    })
    @TestPropertySource(properties = "lifepilot.security.auth-enabled=true")
    class JwtEnabledSecurity {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AgentService agentService;

        @Test
        void stillExposesHealthEndpointWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        void requiresAuthenticationForChatWhenAuthIsEnabled() throws Exception {
            mockMvc.perform(post("/api/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ChatRequest(UUID.randomUUID(), "Create a task")
                            )))
                    .andExpect(status().isUnauthorized());
        }
    }
}
