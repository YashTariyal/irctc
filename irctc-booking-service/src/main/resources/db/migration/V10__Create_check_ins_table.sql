-- Create check_ins table for automated check-in functionality
CREATE TABLE IF NOT EXISTS check_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    pnr_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    seat_number VARCHAR(10),
    coach_number VARCHAR(10),
    check_in_time TIMESTAMP,
    scheduled_check_in_time TIMESTAMP,
    departure_time TIMESTAMP,
    check_in_method VARCHAR(20),
    failure_reason VARCHAR(500),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_check_ins_booking_id ON check_ins (booking_id);
CREATE INDEX IF NOT EXISTS idx_check_ins_user_id ON check_ins (user_id);
CREATE INDEX IF NOT EXISTS idx_check_ins_status ON check_ins (status);
CREATE INDEX IF NOT EXISTS idx_check_ins_tenant_id ON check_ins (tenant_id);

