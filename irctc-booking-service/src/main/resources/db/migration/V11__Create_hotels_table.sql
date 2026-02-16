-- Create hotels table for hotel information
CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(100) NOT NULL,
    nearest_station_code VARCHAR(10),
    address VARCHAR(500),
    city VARCHAR(20),
    state VARCHAR(20),
    pincode VARCHAR(10),
    phone VARCHAR(15),
    email VARCHAR(100),
    rating DECIMAL(3,1),
    price_per_night DECIMAL(10,2),
    total_rooms INT,
    available_rooms INT,
    amenities VARCHAR(500),
    description VARCHAR(1000),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    partner_hotel_id VARCHAR(100),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for hotel lookups
CREATE INDEX IF NOT EXISTS idx_hotels_location ON hotels (location);
CREATE INDEX IF NOT EXISTS idx_hotels_station_code ON hotels (nearest_station_code);
CREATE INDEX IF NOT EXISTS idx_hotels_rating ON hotels (rating);
CREATE INDEX IF NOT EXISTS idx_hotels_tenant_id ON hotels (tenant_id);

