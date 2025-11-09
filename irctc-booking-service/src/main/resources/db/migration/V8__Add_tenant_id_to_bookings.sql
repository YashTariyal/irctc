-- Add tenant_id column to bookings table for multi-tenancy support

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_bookings_tenant_id ON bookings(tenant_id);

-- Add tenant_id to passengers table as well
ALTER TABLE passengers ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_passengers_tenant_id ON passengers(tenant_id);

