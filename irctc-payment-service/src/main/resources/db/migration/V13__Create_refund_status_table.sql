-- Create refund_status table to back RefundStatus entity
CREATE TABLE IF NOT EXISTS refund_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    refund_id VARCHAR(100),
    gateway_refund_id VARCHAR(100),
    gateway_name VARCHAR(50),
    reason VARCHAR(500),
    refund_policy_applied VARCHAR(100),
    refund_percentage DECIMAL(5, 2),
    cancellation_time TIMESTAMP,
    initiated_at TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    reconciliation_status VARCHAR(50),
    reconciled_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes to match JPA mappings
CREATE INDEX IF NOT EXISTS idx_refund_status_payment_id ON refund_status(payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_status_tenant_id ON refund_status(tenant_id);
CREATE INDEX IF NOT EXISTS idx_refund_status_status ON refund_status(status);

