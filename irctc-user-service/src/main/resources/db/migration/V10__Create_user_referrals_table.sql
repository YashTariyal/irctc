CREATE TABLE IF NOT EXISTS user_referrals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referrer_user_id BIGINT NOT NULL,
    referred_user_id BIGINT,
    referral_code_used VARCHAR(32),
    booking_id BIGINT,
    status VARCHAR(40) NOT NULL,
    reward_points INT DEFAULT 0,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_referrals_referrer FOREIGN KEY (referrer_user_id) REFERENCES simple_users(id),
    CONSTRAINT fk_user_referrals_referred FOREIGN KEY (referred_user_id) REFERENCES simple_users(id)
);

CREATE INDEX IF NOT EXISTS idx_user_referrals_referrer ON user_referrals(referrer_user_id);
CREATE INDEX IF NOT EXISTS idx_user_referrals_referred ON user_referrals(referred_user_id);
CREATE INDEX IF NOT EXISTS idx_user_referrals_status ON user_referrals(status);

