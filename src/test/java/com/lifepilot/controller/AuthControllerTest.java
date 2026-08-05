package com.lifepilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.api.GlobalExceptionHandler;
import com.lifepilot.controller.dto.LoginRequest;
import com.lifepilot.controller.dto.RegisterRequest;
import com.lifepilot.service.AuthService;
import com.lifepilot.service.CaptchaService;
import com.lifepilot.service.dto.CaptchaView;
import com.lifepilot.service.dto.LoginResult;
import com.lifepilot.service.dto.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CaptchaService captchaService;

    @Test
    void returnsCaptcha() throws Exception {
        when(captchaService.create()).thenReturn(new CaptchaView("captcha-1", "data:image/png;base64,abc"));

        mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.captchaId").value("captcha-1"));
    }

    @Test
    void logsInWithValidCredentials() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.login(eq("alice"), eq("secret123"), eq("captcha-1"), eq("12")))
                .thenReturn(new LoginResult(
                        "token-abc",
                        new UserProfile(userId, "alice", "Alice")
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice", "secret123", "captcha-1", "12")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token-abc"))
                .andExpect(jsonPath("$.data.user.username").value("alice"));
    }

    @Test
    void rejectsBlankLoginFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("", "", "", "")
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void registersUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.register(eq("alice"), eq("secret123"), eq("Alice"), eq("captcha-1"), eq("12")))
                .thenReturn(new UserProfile(userId, "alice", "Alice"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("alice", "secret123", "Alice", "captcha-1", "12")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void returnsCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.currentUser()).thenReturn(new UserProfile(userId, "alice", "Alice"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }
}
