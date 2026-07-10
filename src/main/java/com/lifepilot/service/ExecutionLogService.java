package com.lifepilot.service;

import com.lifepilot.domain.ExecutionLog;
import com.lifepilot.repository.ExecutionLogRepository;
import com.lifepilot.service.dto.ExecutionLogView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 记录并查询 Agent 与工具执行日志的应用服务。
 */
@Service
public class ExecutionLogService {

    private final ExecutionLogRepository executionLogRepository;

    /**
     * 创建执行日志应用服务。
     *
     * @param executionLogRepository 执行日志持久化访问入口
     */
    public ExecutionLogService(ExecutionLogRepository executionLogRepository) {
        this.executionLogRepository = executionLogRepository;
    }

    /**
     * 记录成功执行的操作。
     *
     * @param conversationId 可选的关联会话标识
     * @param actionType 操作类型
     * @param input 执行输入
     * @param output 执行输出
     * @return 已记录的执行日志视图
     */
    @Transactional
    public ExecutionLogView recordSuccess(UUID conversationId, String actionType, String input, String output) {
        return ExecutionLogView.from(executionLogRepository.save(
                ExecutionLog.success(conversationId, actionType, input, output)
        ));
    }

    /**
     * 记录执行失败的操作。
     *
     * @param conversationId 可选的关联会话标识
     * @param actionType 操作类型
     * @param input 执行输入
     * @param errorMessage 失败原因
     * @return 已记录的执行日志视图
     */
    @Transactional
    public ExecutionLogView recordFailure(UUID conversationId, String actionType, String input, String errorMessage) {
        return ExecutionLogView.from(executionLogRepository.save(
                ExecutionLog.failure(conversationId, actionType, input, errorMessage)
        ));
    }

    /**
     * 按创建时间倒序列出最近执行日志。
     *
     * @return 执行日志视图列表
     */
    @Transactional(readOnly = true)
    public List<ExecutionLogView> listRecent() {
        return executionLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ExecutionLogView::from)
                .toList();
    }
}
