package com.lifepilot.agent;

import com.lifepilot.agent.dto.ReviewDraft;
import com.lifepilot.agent.dto.ReviewDraftInput;

/**
 * 可替换的 AI 复盘草稿生成客户端。
 */
public interface ReviewDraftClient {

    ReviewDraft draft(ReviewDraftInput input);
}
