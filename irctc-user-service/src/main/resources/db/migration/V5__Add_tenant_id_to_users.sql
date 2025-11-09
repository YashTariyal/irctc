-- Add tenant_id column to user tables for multi-tenancy support

ALTER TABLE simple_users ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON simple_users(tenant_id);

ALTER TABLE notification_preferences ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_notification_prefs_tenant_id ON notification_preferences(tenant_id);

