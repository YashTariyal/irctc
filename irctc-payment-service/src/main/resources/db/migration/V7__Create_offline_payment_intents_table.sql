CREATE TABLE IF NOT EXISTS offline_payment_intents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    gateway_preference VARCHAR(50),
    status VARCHAR(40) NOT NULL,
    failure_reason TEXT,
    metadata TEXT,
    processed_payment_id BIGINT,
    queued_at TIMESTAMP,
    last_attempt_at TIMESTAMP,
    processed_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_offline_payment_status CHECK (status IN ('QUEUED','PROCESSING','COMPLETED','FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_offline_payment_user ON offline_payment_intents(user_id);
CREATE INDEX IF NOT EXISTS idx_offline_payment_status ON offline_payment_intents(status);
CREATE INDEX IF NOT EXISTS idx_offline_payment_tenant ON offline_payment_intents(tenant_id);

