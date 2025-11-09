-- Add tenant_id column to trains table for multi-tenancy support

ALTER TABLE trains ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_trains_tenant_id ON trains(tenant_id);

