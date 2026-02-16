-- Create user_consents table for GDPR consent management
CREATE TABLE IF NOT EXISTS user_consents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    granted BOOLEAN NOT NULL,
    purpose VARCHAR(1000),
    version VARCHAR(500),
    granted_at TIMESTAMP,
    withdrawn_at TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_consent UNIQUE (user_id, consent_type)
);

-- Indexes for consent queries
CREATE INDEX IF NOT EXISTS idx_consents_user_id ON user_consents (user_id);
CREATE INDEX IF NOT EXISTS idx_consents_consent_type ON user_consents (consent_type);
CREATE INDEX IF NOT EXISTS idx_consents_tenant_id ON user_consents (tenant_id);

