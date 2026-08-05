package com.lifepilot.config;

import com.lifepilot.tool.NoteTool;
import com.lifepilot.tool.DateTimeTool;
import com.lifepilot.tool.PlanPreviewTool;
import com.lifepilot.tool.TodoTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 配置 Spring AI 对话客户端可用的业务工具。
 */
@Configuration
public class AiToolConfig {

    /**
     * 创建 AI 工具配置。
     */
    public AiToolConfig() {
    }

    /**
     * 创建默认挂载待办和笔记工具的对话客户端。
     *
     * @param chatClientBuilder Spring AI 对话客户端构建器
     * @param todoTool 待办事项工具
     * @param noteTool 笔记工具
     * @return 已注册业务工具的对话客户端
     */
    @Bean
    public ChatClient lifePilotChatClient(
            ChatClient.Builder chatClientBuilder,
            TodoTool todoTool,
            NoteTool noteTool,
            DateTimeTool dateTimeTool,
            PlanPreviewTool planPreviewTool
    ) {
        return chatClientBuilder.defaultTools(todoTool, noteTool, dateTimeTool, planPreviewTool).build();
    }

    /**
     * 提供使用系统默认时区的时钟，便于日期工具读取当前时间。
     *
     * @return 系统时钟
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
