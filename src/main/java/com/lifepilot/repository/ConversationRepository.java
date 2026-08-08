package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.Conversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link Conversation} 实体的持久化访问入口。
 */
@Mapper
public interface ConversationRepository extends MyBatisRepository<Conversation> {

    default Conversation save(Conversation conversation) {
        return save(conversation, Conversation::getId);
    }

    default Optional<Conversation> findById(UUID id) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(Conversation.class)
                .eq(Conversation::getId, id)));
    }
}
