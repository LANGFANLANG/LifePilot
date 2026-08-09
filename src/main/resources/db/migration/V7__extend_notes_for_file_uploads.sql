ALTER TABLE notes
    ADD COLUMN source_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    ADD COLUMN original_filename VARCHAR(255),
    ADD COLUMN content_type VARCHAR(120),
    ADD COLUMN stored_filename VARCHAR(255),
    ADD COLUMN file_size BIGINT;
