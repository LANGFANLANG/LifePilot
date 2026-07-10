package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.CreateNoteRequest;
import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteView;
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
 * 提供笔记创建、查询和详情获取操作的 HTTP 接口。
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    /**
     * 创建笔记接口控制器。
     *
     * @param noteService 笔记应用服务
     */
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 创建笔记。
     *
     * @param request 已校验的笔记创建请求
     * @return 包含已创建笔记的成功响应
     */
    @PostMapping
    public Result<NoteView> create(@Valid @RequestBody CreateNoteRequest request) {
        return Result.success(noteService.create(new CreateNoteCommand(request.title(), request.content())));
    }

    /**
     * 查询全部笔记。
     *
     * @return 包含排序后笔记的成功响应
     */
    @GetMapping
    public Result<List<NoteView>> list() {
        return Result.success(noteService.list());
    }

    /**
     * 根据标识获取笔记详情。
     *
     * @param id 笔记标识
     * @return 包含目标笔记的成功响应
     */
    @GetMapping("/{id}")
    public Result<NoteView> get(@PathVariable UUID id) {
        return Result.success(noteService.get(id));
    }
}
