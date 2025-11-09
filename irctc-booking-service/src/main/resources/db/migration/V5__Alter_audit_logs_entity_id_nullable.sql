-- Alter audit_logs table to make entity_id nullable
-- This allows audit logs for CREATE operations where entity doesn't exist yet
ALTER TABLE audit_logs ALTER COLUMN entity_id BIGINT NULL;

