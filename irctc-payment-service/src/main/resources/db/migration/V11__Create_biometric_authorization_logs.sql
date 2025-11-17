CREATE TABLE IF NOT EXISTS biometric_authorization_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT,
    user_id BIGINT NOT NULL,
    verification_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2),
    currency VARCHAR(10),
    message TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bio_auth_user ON biometric_authorization_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_bio_auth_status ON biometric_authorization_logs(status);

