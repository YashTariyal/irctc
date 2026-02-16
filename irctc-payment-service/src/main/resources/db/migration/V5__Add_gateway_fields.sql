-- Add gateway-related fields to payments table
ALTER TABLE payments 
    ADD COLUMN IF NOT EXISTS gateway_name VARCHAR(50);

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS gateway_transaction_id VARCHAR(255);

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS gateway_fee DOUBLE PRECISION;

-- Create index on gateway_name for faster queries
CREATE INDEX IF NOT EXISTS idx_payments_gateway_name ON payments(gateway_name);

-- Create gateway_statistics table
CREATE TABLE IF NOT EXISTS gateway_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gateway_name VARCHAR(50) NOT NULL UNIQUE,
    total_transactions BIGINT NOT NULL DEFAULT 0,
    successful_transactions BIGINT NOT NULL DEFAULT 0,
    failed_transactions BIGINT NOT NULL DEFAULT 0,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_fees DECIMAL(15, 2) NOT NULL DEFAULT 0,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    last_transaction_time TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create index on gateway_name for faster queries
CREATE INDEX IF NOT EXISTS idx_gateway_stats_name ON gateway_statistics(gateway_name);

