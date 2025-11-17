CREATE TABLE IF NOT EXISTS upi_payment_intents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    booking_id BIGINT,
    user_id BIGINT,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    vpa VARCHAR(100) NOT NULL,
    utr VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    qr_payload TEXT,
    expires_at TIMESTAMP,
    processed_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_upi_order UNIQUE (order_id)
);

CREATE INDEX IF NOT EXISTS idx_upi_status ON upi_payment_intents(status);
CREATE INDEX IF NOT EXISTS idx_upi_user ON upi_payment_intents(user_id);

CREATE TABLE IF NOT EXISTS mobile_wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_reference VARCHAR(100) NOT NULL,
    wallet_provider VARCHAR(50) NOT NULL,
    booking_id BIGINT,
    user_id BIGINT,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    device_info TEXT,
    token_last_four VARCHAR(10),
    status VARCHAR(30) NOT NULL,
    processed_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_wallet_reference UNIQUE (wallet_reference)
);

CREATE INDEX IF NOT EXISTS idx_wallet_status ON mobile_wallet_transactions(status);
CREATE INDEX IF NOT EXISTS idx_wallet_user ON mobile_wallet_transactions(user_id);

