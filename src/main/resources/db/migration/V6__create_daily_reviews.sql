CREATE TABLE daily_reviews (
    id UUID PRIMARY KEY,
    review_date DATE NOT NULL UNIQUE,
    completed_summary TEXT,
    unfinished_summary TEXT,
    new_tasks_summary TEXT,
    reflection TEXT,
    tomorrow_plan TEXT,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
