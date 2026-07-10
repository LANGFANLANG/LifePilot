package com.lifepilot.tool;

import com.lifepilot.service.ExecutionLogService;
import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteView;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 面向 AI 助手暴露的笔记工具。
 */
@Component
public class NoteTool {

    private final NoteService noteService;
    private final ExecutionLogService executionLogService;

    /**
     * 创建笔记工具。
     *
     * @param noteService 笔记应用服务
     * @param executionLogService 执行日志应用服务
     */
    public NoteTool(NoteService noteService, ExecutionLogService executionLogService) {
        this.noteService = noteService;
        this.executionLogService = executionLogService;
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
        try {
            NoteView note = noteService.create(new CreateNoteCommand(title, content));
            executionLogService.recordSuccess(null, "note.create", title, note.toString());
            return ToolResult.success("note created", note);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "note.create", title, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 列出全部笔记。
     *
     * @return 工具执行结果
     */
    @Tool(description = "列出全部笔记")
    public ToolResult listNotes() {
        try {
            List<NoteView> notes = noteService.list();
            executionLogService.recordSuccess(null, "note.list", "", notes.toString());
            return ToolResult.success("notes listed", notes);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "note.list", "", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 获取指定笔记。
     *
     * @param id 笔记标识
     * @return 工具执行结果
     */
    @Tool(description = "获取指定笔记")
    public ToolResult getNote(@ToolParam(description = "笔记 ID") UUID id) {
        String input = id.toString();
        try {
            NoteView note = noteService.get(id);
            executionLogService.recordSuccess(null, "note.get", input, note.toString());
            return ToolResult.success("note found", note);
        } catch (RuntimeException ex) {
            executionLogService.recordFailure(null, "note.get", input, ex.getMessage());
            throw ex;
        }
    }
}
