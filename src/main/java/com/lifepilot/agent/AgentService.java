package com.lifepilot.agent;

import com.lifepilot.agent.dto.AgentRequest;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.domain.ChatRole;
import com.lifepilot.memory.ChatMemoryService;
import com.lifepilot.memory.dto.ConversationView;
import com.lifepilot.memory.dto.MessageView;
import com.lifepilot.service.ExecutionLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 编排聊天记忆与 AI 客户端，完成一次 Agent 对话。
 */
@Service
public class AgentService {

    private final ChatMemoryService chatMemoryService;
    private final AiClient aiClient;
    private final ExecutionLogService executionLogService;

    /**
     * 创建 Agent 对话服务。
     *
     * @param chatMemoryService 聊天记忆服务
     * @param aiClient AI 对话客户端
     * @param executionLogService 执行日志应用服务
     */
    public AgentService(
            ChatMemoryService chatMemoryService,
            AiClient aiClient,
            ExecutionLogService executionLogService
    ) {
        this.chatMemoryService = chatMemoryService;
        this.aiClient = aiClient;
        this.executionLogService = executionLogService;
    }

    /**
     * 保存用户消息、调用 AI、保存助手回复，并记录本次 Agent 执行结果。
     *
     * @param request 对话请求
     * @return 包含会话标识与助手回复的对话响应
     */
    public AgentResponse chat(AgentRequest request) {
        UUID conversationId = null;
        try {
            conversationId = resolveConversationId(request);
            chatMemoryService.appendMessage(conversationId, ChatRole.USER, request.message());
            List<MessageView> messages = chatMemoryService.loadRecentMessages(conversationId);
            String response = aiClient.chat(messages);
            chatMemoryService.appendMessage(conversationId, ChatRole.ASSISTANT, response);
            executionLogService.recordSuccess(conversationId, "agent.chat", request.message(), response);
            return new AgentResponse(conversationId, response);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(conversationId, "agent.chat", request.message(), ex.getMessage());
            throw ex;
        }
    }

    private UUID resolveConversationId(AgentRequest request) {
        if (request.conversationId() != null) {
            return request.conversationId();
        }
        ConversationView conversation = chatMemoryService.createConversation(request.message());
        return conversation.id();
    }
}
