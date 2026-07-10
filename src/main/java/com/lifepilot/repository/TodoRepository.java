package com.lifepilot.repository;

import com.lifepilot.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * {@link Todo} 实体的持久化访问入口。
 */
public interface TodoRepository extends JpaRepository<Todo, UUID> {
}
