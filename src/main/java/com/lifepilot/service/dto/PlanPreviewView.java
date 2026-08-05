package com.lifepilot.service.dto;

import com.lifepilot.domain.PlanPreview;
import com.lifepilot.domain.PlanPreviewStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI 计划草案的展示数据。
 */
public record PlanPreviewView(
        UUID id,
        UUID conversationId,
        String goal,
        PlanPreviewStatus status,
        List<PlanPreviewTaskView> tasks,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PlanPreviewView from(PlanPreview preview) {
        return new PlanPreviewView(
                preview.getId(),
                preview.getConversationId(),
                preview.getGoal(),
                preview.getStatus(),
                preview.getTasks().stream()
                        .map(PlanPreviewTaskView::from)
                        .toList(),
                preview.getCreatedAt(),
                preview.getUpdatedAt()
        );
    }
}
