package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanPreviewTest {

    @Test
    void createsPendingPreviewWithDraftTasks() {
        PlanPreview preview = PlanPreview.create(
                UUID.randomUUID(),
                "Launch a blog",
                List.of(task("Choose blog platform"))
        );

        assertThat(preview.getStatus()).isEqualTo(PlanPreviewStatus.PENDING);
        assertThat(preview.getTasks()).hasSize(1);
        assertThat(preview.getCreatedAt()).isNotNull();
        assertThat(preview.getUpdatedAt()).isNotNull();
    }

    @Test
    void marksPreviewConfirmed() {
        PlanPreview preview = PlanPreview.create(UUID.randomUUID(), "Launch a blog", List.of(task("Choose platform")));

        preview.confirm();

        assertThat(preview.getStatus()).isEqualTo(PlanPreviewStatus.CONFIRMED);
    }

    @Test
    void rejectsAlreadyRejectedPreviewConfirmation() {
        PlanPreview preview = PlanPreview.create(UUID.randomUUID(), "Launch a blog", List.of(task("Choose platform")));
        preview.reject();

        assertThatThrownBy(preview::confirm)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plan preview is not pending");
    }

    @Test
    void requiresAtLeastOneDraftTask() {
        assertThatThrownBy(() -> PlanPreview.create(UUID.randomUUID(), "Launch a blog", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plan preview tasks are required");
    }

    private static PlanPreviewTask task(String title) {
        return PlanPreviewTask.create(title, null, null, TodoPriority.MEDIUM, "work", 30, null, null, 0);
    }
}
