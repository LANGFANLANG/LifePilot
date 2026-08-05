package com.lifepilot.agent;

import com.lifepilot.agent.dto.ReviewDraft;
import com.lifepilot.agent.dto.ReviewDraftInput;
import org.springframework.stereotype.Component;

/**
 * 默认不调用外部模型的复盘客户端，保留服务层确定性草稿。
 */
@Component
public class NoopReviewDraftClient implements ReviewDraftClient {

    @Override
    public ReviewDraft draft(ReviewDraftInput input) {
        throw new UnsupportedOperationException("AI review drafting is not configured");
    }
}
