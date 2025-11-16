-- Create social_accounts table for storing linked social accounts
CREATE TABLE IF NOT EXISTS social_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    provider_name VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
    id_token TEXT,
    token_expires_at TIMESTAMP,
    picture_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    linked_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_social_accounts_user FOREIGN KEY (user_id) REFERENCES simple_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_social_accounts_provider_user UNIQUE (provider, provider_user_id)
);

-- Create indexes
CREATE INDEX idx_social_accounts_user_id ON social_accounts(user_id);
CREATE INDEX idx_social_accounts_provider_id ON social_accounts(provider, provider_user_id);
CREATE INDEX idx_social_accounts_tenant_id ON social_accounts(tenant_id);

