package com.lifepilot.controller;

import com.lifepilot.agent.AgentService;
import com.lifepilot.agent.dto.AgentRequest;
import com.lifepilot.agent.dto.AgentResponse;
import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.ChatRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供 Agent 对话的 HTTP 接口。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AgentService agentService;

    /**
     * 创建 Agent 对话接口控制器。
     *
     * @param agentService Agent 对话服务
     */
    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 提交一条用户消息并返回助手回复。
     *
     * @param request 已校验的对话请求
     * @return 包含会话标识和助手回复的成功响应
     */
    @PostMapping
    public Result<AgentResponse> chat(@Valid @RequestBody ChatRequest request) {
        return Result.success(agentService.chat(new AgentRequest(request.conversationId(), request.message())));
    }
}
