package com.lifepilot.config;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.agent.AgentService;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.controller.ChatController;
import com.lifepilot.controller.dto.ChatRequest;
import com.lifepilot.memory.ChatMemoryService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证 Spring Security 只作为过滤器链基线，不拦截业务请求；API 鉴权由 Sa-Token 拦截器完成。
 */
class SecurityConfigTest {

    @Nested
    @WebMvcTest(value = ChatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SaTokenConfig.class))
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

        @MockBean
        private ChatMemoryService chatMemoryService;

        @Test
        void exposesHealthEndpoint() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        void allowsChatWithoutAuthenticationWhenAuthIsDisabled() throws Exception {
            UUID conversationId = UUID.randomUUID();
            when(agentService.chat(any())).thenReturn(new AgentResponse(conversationId, "Task created"));

            try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
                stp.when(StpUtil::isLogin).thenReturn(false);

                mockMvc.perform(post("/api/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ChatRequest(conversationId, "Create a task")
                                )))
                        .andExpect(status().isOk());
            }
        }

        @Test
        void allowsLocalFrontendCorsPreflight() throws Exception {
            mockMvc.perform(options("/api/chat")
                            .header("Origin", "http://127.0.0.1:5174")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "authorization,content-type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5174"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }
    }

    @Nested
    @WebMvcTest(value = ChatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SaTokenConfig.class))
    @AutoConfigureMockMvc
    @Import(SecurityConfig.class)
    @ImportAutoConfiguration({
            EndpointAutoConfiguration.class,
            ManagementContextAutoConfiguration.class,
            WebEndpointAutoConfiguration.class,
            HealthEndpointAutoConfiguration.class
    })
    @TestPropertySource(properties = "lifepilot.security.auth-enabled=true")
    class EnabledSecurityBaseline {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AgentService agentService;

        @MockBean
        private ChatMemoryService chatMemoryService;

        @Test
        void exposesHealthEndpoint() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        void doesNotBlockApiThroughSpringSecurityWhenAuthIsEnabled() throws Exception {
            when(agentService.chat(any())).thenReturn(new AgentResponse(UUID.randomUUID(), "Task created"));

            try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
                stp.when(StpUtil::isLogin).thenReturn(false);

                mockMvc.perform(post("/api/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ChatRequest(UUID.randomUUID(), "Create a task")
                                )))
                        .andExpect(status().isOk());
            }
        }

        @Test
        void allowsTunnelFrontendCorsPreflightWhenAuthIsEnabled() throws Exception {
            mockMvc.perform(options("/api/chat")
                            .header("Origin", "https://1b9610c.r19.cpolar.top")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "authorization,content-type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "https://1b9610c.r19.cpolar.top"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }
    }
}
