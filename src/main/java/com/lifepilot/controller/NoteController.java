package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.CreateNoteRequest;
import com.lifepilot.controller.dto.UpdateNoteRequest;
import com.lifepilot.service.NoteService;
import com.lifepilot.service.dto.CreateNoteCommand;
import com.lifepilot.service.dto.NoteFileLinkView;
import com.lifepilot.service.dto.NoteView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * 更新笔记。
     *
     * @param id 笔记标识
     * @param request 更新请求
     * @return 更新后的笔记
     */
    @PutMapping("/{id}")
    public Result<NoteView> update(@PathVariable UUID id, @Valid @RequestBody UpdateNoteRequest request) {
        return Result.success(noteService.update(id, new CreateNoteCommand(request.title(), request.content())));
    }

    /**
     * 上传文件并创建文件笔记。
     *
     * @param file 上传的笔记文件
     * @return 包含已创建笔记的成功响应
     */
    @PostMapping("/upload")
    public Result<NoteView> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(noteService.upload(file));
    }

    /**
     * 替换文件笔记的原文件。
     *
     * @param id 笔记标识
     * @param file 新文件
     * @return 更新后的笔记
     */
    @PutMapping("/{id}/file")
    public Result<NoteView> replaceFile(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return Result.success(noteService.replaceFile(id, file));
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

    /**
     * 获取上传笔记原文件的临时访问链接。
     *
     * @param id 笔记标识
     * @param download 是否生成下载链接
     * @return 临时访问链接
     */
    @GetMapping("/{id}/file-url")
    public Result<NoteFileLinkView> fileUrl(
            @PathVariable UUID id,
            @RequestParam(name = "download", defaultValue = "false") boolean download
    ) {
        return Result.success(noteService.fileLink(id, download));
    }

    /**
     * 删除笔记。
     *
     * @param id 笔记标识
     * @return 空成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        noteService.delete(id);
        return Result.success(null);
    }
}
