package com.lifepilot.repository;

import com.lifepilot.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * {@link Conversation} 实体的持久化访问入口。
 */
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}
