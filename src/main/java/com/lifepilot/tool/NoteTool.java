package com.lifepilot.tool;

import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.CreateNoteCommand;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 面向 AI 助手暴露的笔记工具。
 */
@Component
public class NoteTool {

    private final NoteService noteService;

    /**
     * 创建笔记工具。
     *
     * @param noteService 笔记应用服务
     */
    public NoteTool(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 创建新的笔记。
     *
     * @param title 笔记标题
     * @param content 笔记内容
     * @return 工具执行结果
     */
    @Tool(description = "创建新的笔记")
    public ToolResult createNote(
            @ToolParam(description = "笔记标题") String title,
            @ToolParam(description = "笔记内容") String content
    ) {
        return ToolResult.success("note created", noteService.create(new CreateNoteCommand(title, content)));
    }

    /**
     * 列出全部笔记。
     *
     * @return 工具执行结果
     */
    @Tool(description = "列出全部笔记")
    public ToolResult listNotes() {
        return ToolResult.success("notes listed", noteService.list());
    }

    /**
     * 获取指定笔记。
     *
     * @param id 笔记标识
     * @return 工具执行结果
     */
    @Tool(description = "获取指定笔记")
    public ToolResult getNote(@ToolParam(description = "笔记 ID") UUID id) {
        return ToolResult.success("note found", noteService.get(id));
    }
}
