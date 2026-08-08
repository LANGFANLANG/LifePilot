package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;
import org.apache.ibatis.annotations.Mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link Todo} 实体的持久化访问入口。
 */
@Mapper
public interface TodoRepository extends MyBatisRepository<Todo> {

    default List<Todo> findByStatus(TodoStatus status) {
        return selectList(Wrappers.lambdaQuery(Todo.class)
                .eq(Todo::getStatus, status));
    }

    default List<Todo> findByStatusAndReminderAtLessThanEqual(TodoStatus status, OffsetDateTime reminderAt) {
        return selectList(Wrappers.lambdaQuery(Todo.class)
                .eq(Todo::getStatus, status)
                .le(Todo::getReminderAt, reminderAt));
    }

    default List<Todo> findByCompletedAtBetween(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
        return selectList(Wrappers.lambdaQuery(Todo.class)
                .ge(Todo::getCompletedAt, startInclusive)
                .lt(Todo::getCompletedAt, endExclusive));
    }

    default List<Todo> findByStatusAndPlannedStartAtBetween(
            TodoStatus status,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        return selectList(Wrappers.lambdaQuery(Todo.class)
                .eq(Todo::getStatus, status)
                .ge(Todo::getPlannedStartAt, startInclusive)
                .lt(Todo::getPlannedStartAt, endExclusive));
    }

    default List<Todo> findByCreatedAtBetween(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
        return selectList(Wrappers.lambdaQuery(Todo.class)
                .ge(Todo::getCreatedAt, startInclusive)
                .lt(Todo::getCreatedAt, endExclusive));
    }

    default Todo save(Todo todo) {
        return save(todo, Todo::getId);
    }

    default Optional<Todo> findById(UUID id) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(Todo.class)
                .eq(Todo::getId, id)));
    }

    default boolean existsById(UUID id) {
        return selectCount(Wrappers.lambdaQuery(Todo.class)
                .eq(Todo::getId, id)) > 0;
    }

    default void deleteById(UUID id) {
        delete(Wrappers.lambdaQuery(Todo.class)
                .eq(Todo::getId, id));
    }
}
