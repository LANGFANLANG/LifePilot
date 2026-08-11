ALTER TABLE conversations
    ADD COLUMN user_id UUID REFERENCES user_accounts(id);

CREATE INDEX idx_conversations_user_updated
    ON conversations(user_id, updated_at DESC);
