-- Add tenant_id column to notifications table for multi-tenancy support

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_notifications_tenant_id ON notifications(tenant_id);

