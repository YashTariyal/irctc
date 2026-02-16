-- Create refund_policies table to back RefundPolicy entity
CREATE TABLE IF NOT EXISTS refund_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    hours_before_departure INT NOT NULL,
    refund_percentage DECIMAL(5, 2) NOT NULL,
    fixed_charges DECIMAL(10, 2),
    gateway_fee_refundable BOOLEAN,
    active BOOLEAN NOT NULL,
    priority INT NOT NULL,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes to match JPA mappings
CREATE INDEX IF NOT EXISTS idx_refund_policies_tenant_id ON refund_policies(tenant_id);
CREATE INDEX IF NOT EXISTS idx_refund_policies_active ON refund_policies(active);

