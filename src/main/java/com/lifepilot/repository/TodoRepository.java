package com.lifepilot.repository;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * {@link Todo} 实体的持久化访问入口。
 */
public interface TodoRepository extends JpaRepository<Todo, UUID> {

    List<Todo> findByStatus(TodoStatus status);

    List<Todo> findByStatusAndReminderAtLessThanEqual(TodoStatus status, OffsetDateTime reminderAt);

    List<Todo> findByCompletedAtBetween(OffsetDateTime startInclusive, OffsetDateTime endExclusive);

    List<Todo> findByStatusAndPlannedStartAtBetween(
            TodoStatus status,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    );

    List<Todo> findByCreatedAtBetween(OffsetDateTime startInclusive, OffsetDateTime endExclusive);
}
