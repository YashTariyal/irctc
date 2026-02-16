-- Create hotel_bookings table for hotel reservations
CREATE TABLE IF NOT EXISTS hotel_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    train_booking_id BIGINT,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_rooms INT NOT NULL,
    number_of_guests INT NOT NULL,
    guest_name VARCHAR(100) NOT NULL,
    guest_email VARCHAR(100),
    guest_phone VARCHAR(15),
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2),
    final_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20),
    is_package_deal BOOLEAN DEFAULT FALSE,
    cancellation_policy VARCHAR(500),
    special_requests VARCHAR(1000),
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE RESTRICT
);

-- Indexes for hotel bookings
CREATE INDEX IF NOT EXISTS idx_hotel_bookings_user_id ON hotel_bookings (user_id);
CREATE INDEX IF NOT EXISTS idx_hotel_bookings_hotel_id ON hotel_bookings (hotel_id);
CREATE INDEX IF NOT EXISTS idx_hotel_bookings_booking_id ON hotel_bookings (train_booking_id);
CREATE INDEX IF NOT EXISTS idx_hotel_bookings_status ON hotel_bookings (status);
CREATE INDEX IF NOT EXISTS idx_hotel_bookings_tenant_id ON hotel_bookings (tenant_id);

