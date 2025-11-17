CREATE TABLE IF NOT EXISTS biometric_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    biometric_type VARCHAR(50) NOT NULL,
    template_hash VARCHAR(256) NOT NULL,
    public_key TEXT,
    device_info TEXT,
    status VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP,
    last_verified_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_biometric_user_device UNIQUE (user_id, device_id),
    CONSTRAINT fk_biometric_user FOREIGN KEY (user_id) REFERENCES simple_users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_biometric_user ON biometric_credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_biometric_status ON biometric_credentials(status);
CREATE INDEX IF NOT EXISTS idx_biometric_tenant ON biometric_credentials(tenant_id);

