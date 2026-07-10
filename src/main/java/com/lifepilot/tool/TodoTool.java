package com.lifepilot.tool;

import com.lifepilot.service.TodoService;
import com.lifepilot.service.dto.CreateTodoCommand;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 面向 AI 助手暴露的待办事项工具。
 */
@Component
public class TodoTool {

    private final TodoService todoService;

    /**
     * 创建待办事项工具。
     *
     * @param todoService 待办事项应用服务
     */
    public TodoTool(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 创建新的待办事项。
     *
     * @param title 待办事项标题
     * @param description 可选的待办事项详情
     * @param dueAt 可选的截止时间
     * @return 工具执行结果
     */
    @Tool(description = "创建新的待办事项")
    public ToolResult createTodo(
            @ToolParam(description = "待办事项标题") String title,
            @ToolParam(required = false, description = "待办事项详情") String description,
            @ToolParam(required = false, description = "截止时间，使用 ISO-8601 日期时间格式") OffsetDateTime dueAt
    ) {
        return ToolResult.success("todo created", todoService.create(new CreateTodoCommand(title, description, dueAt)));
    }

    /**
     * 列出全部待办事项。
     *
     * @return 工具执行结果
     */
    @Tool(description = "列出全部待办事项")
    public ToolResult listTodos() {
        return ToolResult.success("todos listed", todoService.list());
    }

    /**
     * 将指定待办事项标记为已完成。
     *
     * @param id 待办事项标识
     * @return 工具执行结果
     */
    @Tool(description = "将指定待办事项标记为已完成")
    public ToolResult completeTodo(@ToolParam(description = "待办事项 ID") UUID id) {
        return ToolResult.success("todo completed", todoService.complete(id));
    }
}
