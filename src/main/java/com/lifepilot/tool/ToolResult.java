package com.lifepilot.tool;

/**
 * AI 工具执行的统一结果。
 *
 * @param success 工具执行是否成功
 * @param message 面向模型或调用方的结果消息
 * @param data 可选的结构化结果数据
 */
public record ToolResult(boolean success, String message, Object data) {

    /**
     * 创建成功的工具执行结果。
     *
     * @param message 结果消息
     * @param data 结构化结果数据
     * @return 成功的工具执行结果
     */
    public static ToolResult success(String message, Object data) {
        return new ToolResult(true, message, data);
    }

    /**
     * 创建不含数据的失败工具执行结果。
     *
     * @param message 失败消息
     * @return 失败的工具执行结果
     */
    public static ToolResult failure(String message) {
        return new ToolResult(false, message, null);
    }
}
