package com.lifepilot.service.dto;

import com.lifepilot.domain.ExecutionLog;
import com.lifepilot.domain.ExecutionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 执行日志对外展示数据。
 *
 * @param id 执行日志标识
 * @param conversationId 可选的关联会话标识
 * @param actionType 操作类型
 * @param input 执行输入
 * @param output 执行输出
 * @param status 执行状态
 * @param errorMessage 失败原因
 * @param createdAt 日志创建时间
 */
public record ExecutionLogView(
        UUID id,
        UUID conversationId,
        String actionType,
        String input,
        String output,
        ExecutionStatus status,
        String errorMessage,
        OffsetDateTime createdAt
) {

    /**
     * 将执行日志领域实体转换为展示数据。
     *
     * @param executionLog 执行日志领域实体
     * @return 执行日志展示数据
     */
    public static ExecutionLogView from(ExecutionLog executionLog) {
        return new ExecutionLogView(
                executionLog.getId(),
                executionLog.getConversationId(),
                executionLog.getActionType(),
                executionLog.getInput(),
                executionLog.getOutput(),
                executionLog.getStatus(),
                executionLog.getErrorMessage(),
                executionLog.getCreatedAt()
        );
    }
}
