CREATE TABLE IF NOT EXISTS payment_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    down_payment DECIMAL(12, 2),
    emi_amount DECIMAL(12, 2),
    interest_rate DECIMAL(5, 2),
    installments INT NOT NULL,
    frequency VARCHAR(20) DEFAULT 'MONTHLY',
    status VARCHAR(40) NOT NULL,
    start_date DATE,
    end_date DATE,
    gateway_reference VARCHAR(100),
    notes TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_payment_plan_status CHECK (status IN ('ACTIVE','COMPLETED','DEFAULTED','CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_payment_plans_user ON payment_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_plans_booking ON payment_plans(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_plans_status ON payment_plans(status);

CREATE TABLE IF NOT EXISTS emi_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_plan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    amount_due DECIMAL(12, 2) NOT NULL,
    amount_paid DECIMAL(12, 2),
    payment_date DATE,
    status VARCHAR(40) NOT NULL,
    payment_reference VARCHAR(100),
    penalty_amount DECIMAL(12, 2),
    metadata TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_emi_payment_plan FOREIGN KEY (payment_plan_id) REFERENCES payment_plans(id) ON DELETE CASCADE,
    CONSTRAINT chk_emi_payment_status CHECK (status IN ('DUE','PAID','OVERDUE','WAIVED'))
);

CREATE INDEX IF NOT EXISTS idx_emi_payment_plan ON emi_payments(payment_plan_id);
CREATE INDEX IF NOT EXISTS idx_emi_payment_status ON emi_payments(status);

