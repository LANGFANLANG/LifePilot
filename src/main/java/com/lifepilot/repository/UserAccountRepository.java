package com.lifepilot.repository;

import com.lifepilot.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserAccount} 实体的持久化访问入口。
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    /**
     * 按登录账号查询用户。
     *
     * @param username 登录账号
     * @return 匹配的用户；不存在时为空
     */
    Optional<UserAccount> findByUsername(String username);

    /**
     * 判断登录账号是否已被占用。
     *
     * @param username 登录账号
     * @return 已占用时返回 {@code true}
     */
    boolean existsByUsername(String username);
}
