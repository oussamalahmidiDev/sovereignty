CREATE TABLE chats (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    content CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_chats_updated_at ON chats(updated_at DESC);
CREATE INDEX idx_chat_messages_chat_id_created_at ON chat_messages(chat_id, created_at ASC);
