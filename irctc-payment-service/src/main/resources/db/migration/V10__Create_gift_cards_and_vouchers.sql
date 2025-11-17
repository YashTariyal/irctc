CREATE TABLE IF NOT EXISTS gift_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    purchaser_user_id BIGINT,
    recipient_email VARCHAR(150),
    message TEXT,
    initial_amount DECIMAL(12, 2) NOT NULL,
    balance_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gift_card_status ON gift_cards(status);
CREATE INDEX IF NOT EXISTS idx_gift_card_tenant ON gift_cards(tenant_id);

CREATE TABLE IF NOT EXISTS gift_card_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gift_card_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(50),
    CONSTRAINT fk_gift_card_tx_card FOREIGN KEY (gift_card_id) REFERENCES gift_cards(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    voucher_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12, 2) NOT NULL,
    max_discount_amount DECIMAL(12, 2),
    min_order_amount DECIMAL(12, 2),
    usage_limit INT,
    usage_count INT DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP,
    description TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_voucher_status ON vouchers(status);

