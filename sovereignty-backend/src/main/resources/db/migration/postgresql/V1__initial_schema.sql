CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS vector_store
(
    id        uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    content   text,
    metadata  jsonb,
    embedding vector(1024)
);


CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
    ON vector_store USING hnsw (embedding vector_cosine_ops);

CREATE TABLE IF NOT EXISTS documents
(
    id           uuid PRIMARY KEY,
    file_name    varchar(255) NOT NULL,
    content_type varchar(255),
    status       varchar(50) NOT NULL CONSTRAINT documents_status_check CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY')),
    created_at   timestamp NOT NULL
);