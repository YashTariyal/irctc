ALTER TABLE simple_users
    ADD COLUMN IF NOT EXISTS referral_code VARCHAR(32),
    ADD COLUMN IF NOT EXISTS referred_by_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS referral_points INT DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_referral_code ON simple_users(referral_code);
CREATE INDEX IF NOT EXISTS idx_users_referred_by ON simple_users(referred_by_user_id);

