ALTER TABLE todos
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    ADD COLUMN category VARCHAR(80),
    ADD COLUMN estimated_minutes INTEGER,
    ADD COLUMN planned_start_at TIMESTAMPTZ,
    ADD COLUMN reminder_at TIMESTAMPTZ,
    ADD COLUMN completed_at TIMESTAMPTZ,
    ADD COLUMN parent_todo_id UUID REFERENCES todos(id),
    ADD COLUMN source VARCHAR(40) NOT NULL DEFAULT 'manual',
    ADD COLUMN postponement_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE todos
    ADD CONSTRAINT todos_estimated_minutes_non_negative
        CHECK (estimated_minutes IS NULL OR estimated_minutes >= 0),
    ADD CONSTRAINT todos_postponement_count_non_negative
        CHECK (postponement_count >= 0);
