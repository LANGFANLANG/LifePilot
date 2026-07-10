package com.lifepilot.agent;

import com.lifepilot.memory.dto.MessageView;

import java.util.List;

/**
 * 可替换的 AI 对话客户端抽象。
 */
public interface AiClient {

    /**
     * 根据会话记忆生成助手回复。
     *
     * @param messages 按时间排序的会话消息
     * @return 助手回复内容
     */
    String chat(List<MessageView> messages);
}
