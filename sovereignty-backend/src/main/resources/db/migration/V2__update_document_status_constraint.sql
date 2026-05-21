-- Drop the old check constraint
ALTER TABLE documents DROP CONSTRAINT documents_status_check;

-- Add new check constraint with all allowed statuses
ALTER TABLE documents ADD CONSTRAINT documents_status_check
    CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY', 'FAILED'));