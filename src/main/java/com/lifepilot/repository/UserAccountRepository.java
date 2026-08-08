package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserAccount} 实体的持久化访问入口。
 */
@Mapper
public interface UserAccountRepository extends MyBatisRepository<UserAccount> {

    /**
     * 按登录账号查询用户。
     *
     * @param username 登录账号
     * @return 匹配的用户；不存在时为空
     */
    default Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(UserAccount.class)
                .eq(UserAccount::getUsername, username)));
    }

    /**
     * 判断登录账号是否已被占用。
     *
     * @param username 登录账号
     * @return 已占用时返回 {@code true}
     */
    default boolean existsByUsername(String username) {
        return selectCount(Wrappers.lambdaQuery(UserAccount.class)
                .eq(UserAccount::getUsername, username)) > 0;
    }

    default UserAccount save(UserAccount user) {
        return save(user, UserAccount::getId);
    }

    default Optional<UserAccount> findById(UUID id) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(UserAccount.class)
                .eq(UserAccount::getId, id)));
    }
}
