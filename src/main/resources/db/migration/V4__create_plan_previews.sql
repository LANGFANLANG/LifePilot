CREATE TABLE plan_previews (
    id UUID PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id),
    goal TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE plan_preview_tasks (
    id UUID PRIMARY KEY,
    plan_preview_id UUID NOT NULL REFERENCES plan_previews(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_at TIMESTAMPTZ,
    priority VARCHAR(20) NOT NULL,
    category VARCHAR(80),
    estimated_minutes INTEGER,
    planned_start_at TIMESTAMPTZ,
    reminder_at TIMESTAMPTZ,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE plan_preview_tasks
    ADD CONSTRAINT plan_preview_tasks_estimated_minutes_non_negative
        CHECK (estimated_minutes IS NULL OR estimated_minutes >= 0);
