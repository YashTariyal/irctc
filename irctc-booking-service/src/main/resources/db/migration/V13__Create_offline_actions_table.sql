CREATE TABLE IF NOT EXISTS offline_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    action_type VARCHAR(100) NOT NULL,
    payload TEXT,
    status VARCHAR(40) NOT NULL,
    failure_reason TEXT,
    queued_at TIMESTAMP,
    processed_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_offline_actions_status CHECK (status IN ('QUEUED','PROCESSING','COMPLETED','FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_offline_action_user ON offline_actions(user_id);
CREATE INDEX IF NOT EXISTS idx_offline_action_status ON offline_actions(status);
CREATE INDEX IF NOT EXISTS idx_offline_action_tenant ON offline_actions(tenant_id);

