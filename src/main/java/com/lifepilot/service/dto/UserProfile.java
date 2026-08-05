package com.lifepilot.service.dto;

import com.lifepilot.domain.UserAccount;

import java.util.UUID;

/**
 * 登录用户对外展示数据。
 *
 * @param id 用户标识
 * @param username 登录账号
 * @param displayName 显示名称；未设置时为 {@code null}
 */
public record UserProfile(UUID id, String username, String displayName) {

    /**
     * 将用户账号实体转换为展示数据。
     *
     * @param user 用户账号实体
     * @return 用户展示数据
     */
    public static UserProfile from(UserAccount user) {
        return new UserProfile(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
