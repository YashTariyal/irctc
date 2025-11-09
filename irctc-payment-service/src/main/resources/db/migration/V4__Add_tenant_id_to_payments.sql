-- Add tenant_id column to payments table for multi-tenancy support

ALTER TABLE payments ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_payments_tenant_id ON payments(tenant_id);

