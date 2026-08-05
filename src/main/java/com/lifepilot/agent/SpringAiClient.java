package com.lifepilot.agent;

import com.lifepilot.domain.ChatRole;
import com.lifepilot.memory.dto.MessageView;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 Spring AI {@link ChatClient} 的 AI 对话客户端实现。
 */
@Component
public class SpringAiClient implements AiClient {

    static final String SYSTEM_PROMPT = """
            你是 LifePilot，一个个人执行规划助手。
            当用户给出目标时，如果缺少截止时间、范围、优先级或可用时间，请先追问。
            当目标需要拆成多个任务时，先创建计划草案，等待用户确认。
            未经用户明确确认，不要批量创建、删除或修改待办。
            调用工具时使用 ISO-8601 日期时间。
            任务标题要具体、简短，预计耗时要现实。
            """;

    private final ChatClient chatClient;

    /**
     * 创建 Spring AI 对话客户端适配器。
     *
     * @param chatClient Spring AI 聊天客户端
     */
    public SpringAiClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 将持久化会话消息转换为 Spring AI 消息并获取助手回复。
     *
     * @param messages 按时间排序的会话消息
     * @return 助手回复内容
     */
    @Override
    public String chat(List<MessageView> messages) {
        List<Message> promptMessages = new java.util.ArrayList<>();
        promptMessages.add(new SystemMessage(SYSTEM_PROMPT));
        promptMessages.addAll(messages.stream()
                .map(this::toSpringAiMessage)
                .toList());
        return chatClient.prompt()
                .messages(promptMessages)
                .call()
                .content();
    }

    private Message toSpringAiMessage(MessageView message) {
        return switch (message.role()) {
            case ChatRole.USER -> new UserMessage(message.content());
            case ChatRole.ASSISTANT -> new AssistantMessage(message.content());
            case ChatRole.SYSTEM -> new SystemMessage(message.content());
            case ChatRole.TOOL -> new ToolResponseMessage(List.of(
                    new ToolResponseMessage.ToolResponse(
                            message.id().toString(),
                            "lifepilot",
                            message.content()
                    )
            ));
        };
    }
}
