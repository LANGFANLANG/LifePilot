package com.lifepilot.service;

import cn.dev33.satoken.stp.StpUtil;
import com.lifepilot.domain.UserAccount;
import com.lifepilot.repository.UserAccountRepository;
import com.lifepilot.service.dto.LoginResult;
import com.lifepilot.service.dto.UserProfile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 协调注册、登录与当前用户查询用例。
 */
@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    /**
     * 创建登录认证应用服务。
     *
     * @param userAccountRepository 用户账号持久化访问入口
     * @param passwordEncoder 密码哈希编码器
     * @param captchaService 验证码应用服务
     */
    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            CaptchaService captchaService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
    }

    /**
     * 注册新用户并返回其公开资料。
     *
     * @param username 登录账号
     * @param password 明文密码
     * @param displayName 可选的显示名称
     * @param captchaId 验证码标识
     * @param captchaCode 验证码答案
     * @return 已注册用户的公开资料
     * @throws IllegalArgumentException 账号已被占用或验证码无效时抛出
     */
    @Transactional
    public UserProfile register(String username, String password, String displayName, String captchaId, String captchaCode) {
        String normalizedUsername = normalizeUsername(username);
        captchaService.verifyAndConsume(captchaId, captchaCode);
        if (userAccountRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("用户名已被占用");
        }
        UserAccount user = UserAccount.create(
                normalizedUsername,
                passwordEncoder.encode(password),
                displayName == null || displayName.isBlank() ? normalizedUsername : displayName
        );
        return UserProfile.from(userAccountRepository.save(user));
    }

    /**
     * 校验凭据并建立登录会话。
     *
     * @param username 登录账号
     * @param password 明文密码
     * @param captchaId 验证码标识
     * @param captchaCode 验证码答案
     * @return 访问令牌与用户公开资料
     * @throws IllegalArgumentException 凭据或验证码错误时抛出
     */
    @Transactional
    public LoginResult login(String username, String password, String captchaId, String captchaCode) {
        captchaService.verifyAndConsume(captchaId, captchaCode);
        UserAccount user = findEnabledByUsername(normalizeUsername(username));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        return new LoginResult(StpUtil.getTokenValue(), UserProfile.from(user));
    }

    /**
     * 结束当前登录会话。
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 查询当前登录用户的公开资料。
     *
     * @return 当前登录用户的公开资料
     * @throws IllegalArgumentException 会话对应的用户不存在时抛出
     */
    @Transactional(readOnly = true)
    public UserProfile currentUser() {
        UserAccount user = userAccountRepository.findById(UUID.fromString(StpUtil.getLoginIdAsString()))
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return UserProfile.from(user);
    }

    private UserAccount findEnabledByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("请输入用户名");
        }
        return username.trim();
    }
}
