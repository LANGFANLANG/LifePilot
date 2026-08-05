package com.lifepilot.service;

import cn.dev33.satoken.stp.StpUtil;
import com.lifepilot.domain.UserAccount;
import com.lifepilot.repository.UserAccountRepository;
import com.lifepilot.service.dto.LoginResult;
import com.lifepilot.service.dto.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registersUserWithHashedPassword() {
        when(userAccountRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile profile = authService.register("alice", "secret123", "Alice", "captcha-1", "12");

        verify(captchaService).verifyAndConsume("captcha-1", "12");
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("alice");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(profile.username()).isEqualTo("alice");
        assertThat(profile.displayName()).isEqualTo("Alice");
    }

    @Test
    void rejectsDuplicateUsername() {
        when(userAccountRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("alice", "secret123", "Alice", "captcha-1", "12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户名已被占用");
    }

    @Test
    void logsInWithValidCredentialsAndReturnsToken() {
        UserAccount user = UserAccount.create("alice", "hashed", "Alice");
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.getTokenValue()).thenReturn("token-abc");

            LoginResult result = authService.login("alice", "secret123", "captcha-1", "12");

            stp.verify(() -> StpUtil.login(user.getId()));
            assertThat(result.token()).isEqualTo("token-abc");
            assertThat(result.user().username()).isEqualTo("alice");
        }
    }

    @Test
    void rejectsWrongPassword() {
        UserAccount user = UserAccount.create("alice", "hashed", "Alice");
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("alice", "wrong-password", "captcha-1", "12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void rejectsUnknownUsername() {
        when(userAccountRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost", "whatever", "captcha-1", "12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void logsOutCurrentSession() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            authService.logout();
            stp.verify(StpUtil::logout);
        }
    }

    @Test
    void returnsCurrentUserProfile() {
        UserAccount user = UserAccount.create("alice", "hashed", "Alice");
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsString).thenReturn(user.getId().toString());

            UserProfile profile = authService.currentUser();

            assertThat(profile.id()).isEqualTo(user.getId());
            assertThat(profile.username()).isEqualTo("alice");
        }
    }
}
