package com.lifepilot.domain;

/**
 * 聊天消息的发送方角色。
 */
public enum ChatRole {
    /** 用户发送的消息。 */
    USER,
    /** AI 助手发送的消息。 */
    ASSISTANT,
    /** 系统发送的上下文消息。 */
    SYSTEM,
    /** 工具执行产生的消息。 */
    TOOL
}
