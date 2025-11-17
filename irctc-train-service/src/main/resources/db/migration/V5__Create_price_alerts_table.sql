CREATE TABLE IF NOT EXISTS price_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255),
    phone_number VARCHAR(30),
    train_number VARCHAR(50),
    source_station VARCHAR(100),
    destination_station VARCHAR(100),
    travel_date DATE,
    alert_type VARCHAR(50) NOT NULL,
    notification_channel VARCHAR(50),
    target_price DECIMAL(10, 2),
    min_availability INT,
    status VARCHAR(40) NOT NULL,
    recurrence VARCHAR(40),
    last_triggered_at TIMESTAMP,
    metadata TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_price_alerts_user ON price_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_price_alerts_status ON price_alerts(status);
CREATE INDEX IF NOT EXISTS idx_price_alerts_train ON price_alerts(train_number);
CREATE INDEX IF NOT EXISTS idx_price_alerts_tenant ON price_alerts(tenant_id);

