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
    INDEX idx_consents_user_id (user_id),
    INDEX idx_consents_consent_type (consent_type),
    INDEX idx_consents_tenant_id (tenant_id),
    UNIQUE KEY uk_user_consent (user_id, consent_type)
);

