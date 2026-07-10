package com.lifepilot.service.dto;

/**
 * 服务层创建笔记命令。
 *
 * @param title 笔记标题
 * @param content 笔记内容
 */
public record CreateNoteCommand(String title, String content) {
}
