CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    trace_id VARCHAR(255),
    status VARCHAR(50) NOT NULL CONSTRAINT outbox_events_status_check CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);
