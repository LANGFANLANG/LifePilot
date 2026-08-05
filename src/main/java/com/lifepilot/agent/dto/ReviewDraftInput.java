package com.lifepilot.agent.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 生成复盘草稿所需的结构化输入。
 */
public record ReviewDraftInput(
        LocalDate reviewDate,
        List<String> completedTasks,
        List<String> unfinishedTasks,
        List<String> newTasks
) {
}
