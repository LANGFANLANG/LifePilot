package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.Conversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
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

    /**
     * 鎸夋渶杩戞洿鏂版椂闂村€掑簭鏌ヨ鎸囧畾鐢ㄦ埛鐨勪細璇濄€?
     *
     * @param userId 鐢ㄦ埛鏍囪瘑
     * @return 鐢ㄦ埛浼氳瘽鍒楄〃
     */
    default List<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId) {
        var query = Wrappers.lambdaQuery(Conversation.class)
                .orderByDesc(Conversation::getUpdatedAt);
        if (userId == null) {
            query.isNull(Conversation::getUserId);
        } else {
            query.eq(Conversation::getUserId, userId);
        }
        return selectList(query);
    }

    /**
     * 鏌ヨ鎸囧畾鐢ㄦ埛鎷ユ湁鐨勫崟涓細璇濄€?
     *
     * @param id 浼氳瘽鏍囪瘑
     * @param userId 鐢ㄦ埛鏍囪瘑
     * @return 鍖归厤鐨勪細璇?
     */
    default Optional<Conversation> findByIdAndUserId(UUID id, UUID userId) {
        var query = Wrappers.lambdaQuery(Conversation.class)
                .eq(Conversation::getId, id);
        if (userId == null) {
            query.isNull(Conversation::getUserId);
        } else {
            query.eq(Conversation::getUserId, userId);
        }
        return Optional.ofNullable(selectOne(query));
    }
}
