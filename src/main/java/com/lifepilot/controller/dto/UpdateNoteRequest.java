package com.lifepilot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新笔记的 HTTP 请求参数。
 *
 * @param title 笔记标题
 * @param content 笔记内容或文件预览备注
 */
public record UpdateNoteRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {
}
