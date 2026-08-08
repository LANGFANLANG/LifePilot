package com.lifepilot.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 记录 Agent 或工具一次执行过程的持久化日志。
 */
@TableName("execution_logs")
public class ExecutionLog {

    @TableId
    private UUID id;

    private UUID conversationId;

    private String actionType;

    private String input;

    private String output;

    private ExecutionStatus status;

    private String errorMessage;

    private OffsetDateTime createdAt;

    /**
     * 供 JPA 创建实体实例使用。
     */
    protected ExecutionLog() {
    }

    private ExecutionLog(
            UUID id,
            UUID conversationId,
            String actionType,
            String input,
            String output,
            ExecutionStatus status,
            String errorMessage,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.conversationId = conversationId;
        this.actionType = actionType;
        this.input = input;
        this.output = output;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    /**
     * 创建成功执行日志。
     *
     * @param conversationId 可选的会话标识
     * @param actionType 操作类型
     * @param input 输入内容
     * @param output 输出内容
     * @return 成功执行日志
     */
    public static ExecutionLog success(UUID conversationId, String actionType, String input, String output) {
        return new ExecutionLog(
                UUID.randomUUID(),
                conversationId,
                actionType,
                input,
                output,
                ExecutionStatus.SUCCESS,
                null,
                OffsetDateTime.now()
        );
    }

    /**
     * 创建失败执行日志。
     *
     * @param conversationId 可选的会话标识
     * @param actionType 操作类型
     * @param input 输入内容
     * @param errorMessage 失败原因
     * @return 失败执行日志
     */
    public static ExecutionLog failure(UUID conversationId, String actionType, String input, String errorMessage) {
        return new ExecutionLog(
                UUID.randomUUID(),
                conversationId,
                actionType,
                input,
                null,
                ExecutionStatus.FAILURE,
                errorMessage,
                OffsetDateTime.now()
        );
    }

    /**
     * 获取执行日志标识。
     *
     * @return 执行日志标识
     */
    public UUID getId() {
        return id;
    }

    /**
     * 获取关联会话标识。
     *
     * @return 关联会话标识；未关联会话时为 {@code null}
     */
    public UUID getConversationId() {
        return conversationId;
    }

    /**
     * 获取操作类型。
     *
     * @return 操作类型
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * 获取执行输入。
     *
     * @return 执行输入
     */
    public String getInput() {
        return input;
    }

    /**
     * 获取执行输出。
     *
     * @return 执行输出；失败时为 {@code null}
     */
    public String getOutput() {
        return output;
    }

    /**
     * 获取执行状态。
     *
     * @return 执行状态
     */
    public ExecutionStatus getStatus() {
        return status;
    }

    /**
     * 获取失败原因。
     *
     * @return 失败原因；成功时为 {@code null}
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取日志创建时间。
     *
     * @return 日志创建时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
