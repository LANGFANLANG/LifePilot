package com.lifepilot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建笔记的 HTTP 请求参数。
 *
 * @param title 笔记标题，长度不超过 200 个字符
 * @param content 不可为空的笔记内容
 */
public record CreateNoteRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {
}
