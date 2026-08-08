package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 持久化的登录用户账号。
 */
@TableName("user_accounts")
public class UserAccount {

    @TableId
    private UUID id;

    private String username;

    private String passwordHash;

    private String displayName;

    private boolean enabled;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected UserAccount() {
    }

    private UserAccount(
            UUID id,
            String username,
            String passwordHash,
            String displayName,
            boolean enabled,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建启用状态的用户账号。
     *
     * @param username 登录账号
     * @param passwordHash 加密后的密码
     * @param displayName 可选的显示名称
     * @return 新建的用户账号
     */
    public static UserAccount create(String username, String passwordHash, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        return new UserAccount(UUID.randomUUID(), username, passwordHash, displayName, true, now, now);
    }

    /**
     * 获取用户标识。
     *
     * @return 用户标识
     */
    public UUID getId() {
        return id;
    }

    /**
     * 获取登录账号。
     *
     * @return 登录账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取加密后的密码。
     *
     * @return 加密后的密码
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 获取显示名称。
     *
     * @return 显示名称；未设置时为 {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取账号是否可用。
     *
     * @return 可用时返回 {@code true}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取最近修改时间。
     *
     * @return 最近修改时间
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
