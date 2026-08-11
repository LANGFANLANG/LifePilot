package com.lifepilot.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lifepilot.agent.AgentService;
import com.lifepilot.agent.dto.AgentRequest;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.ChatRequest;
import com.lifepilot.memory.ChatMemoryService;
import com.lifepilot.memory.dto.ConversationView;
import com.lifepilot.memory.dto.MessageView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Provides HTTP APIs for AI chat and persisted chat history.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AgentService agentService;
    private final ChatMemoryService chatMemoryService;

    /**
     * Creates the AI chat controller.
     *
     * @param agentService AI chat service
     * @param chatMemoryService persisted chat memory service
     */
    public ChatController(AgentService agentService, ChatMemoryService chatMemoryService) {
        this.agentService = agentService;
        this.chatMemoryService = chatMemoryService;
    }

    /**
     * Lists conversations owned by the current user.
     *
     * @return current user's conversations
     */
    @GetMapping("/conversations")
    public Result<List<ConversationView>> listConversations() {
        return Result.success(chatMemoryService.listConversations(currentUserId()));
    }

    /**
     * Lists messages for a current user's conversation.
     *
     * @param conversationId conversation identifier
     * @return conversation messages
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<MessageView>> listMessages(@PathVariable UUID conversationId) {
        return Result.success(chatMemoryService.loadMessages(currentUserId(), conversationId));
    }

    /**
     * Submits one user message and returns the assistant reply.
     *
     * @param request validated chat request
     * @return response containing conversation id and assistant content
     */
    @PostMapping
    public Result<AgentResponse> chat(@Valid @RequestBody ChatRequest request) {
        return Result.success(agentService.chat(new AgentRequest(
                currentUserId(),
                request.conversationId(),
                request.message()
        )));
    }

    private UUID currentUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return UUID.fromString(StpUtil.getLoginIdAsString());
    }
}
