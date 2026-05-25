ALTER TABLE documents DROP COLUMN trace_id;

CREATE TABLE document_events (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    trace_id VARCHAR(255) NOT NULL,
    failure_reason VARCHAR(2000),
    created_at TIMESTAMP NOT NULL
);
