package com.lifepilot.repository;

import com.lifepilot.domain.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 执行日志持久化访问入口。
 */
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, UUID> {

    /**
     * 按创建时间倒序获取全部执行日志。
     *
     * @return 已排序的执行日志列表
     */
    List<ExecutionLog> findAllByOrderByCreatedAtDesc();
}
