CREATE TABLE reminder_deliveries (
    id UUID PRIMARY KEY,
    todo_id UUID NOT NULL REFERENCES todos(id) ON DELETE CASCADE,
    reminder_at TIMESTAMPTZ NOT NULL,
    channel VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    message TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX reminder_deliveries_unique_todo_time_channel
    ON reminder_deliveries(todo_id, reminder_at, channel);
