-- Booking Service Initial Schema Migration

-- ShedLock table for distributed scheduler locking
CREATE TABLE IF NOT EXISTS shedlock (
  name VARCHAR(64) NOT NULL PRIMARY KEY,
  lock_until TIMESTAMP NOT NULL,
  locked_at TIMESTAMP NOT NULL,
  locked_by VARCHAR(255) NOT NULL
);

-- Outbox Events table for transactional outbox pattern
CREATE TABLE IF NOT EXISTS outbox_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  topic VARCHAR(100) NOT NULL,
  payload TEXT NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  max_retries INT NOT NULL DEFAULT 3,
  error_message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at TIMESTAMP,
  CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_events(status, created_at);

-- Idempotency Keys table
CREATE TABLE IF NOT EXISTS idempotency_keys (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  idempotencyKey VARCHAR(128) NOT NULL UNIQUE,
  httpMethod VARCHAR(16) NOT NULL,
  requestPath VARCHAR(255) NOT NULL,
  requestHash TEXT,
  responseBody TEXT,
  responseStatus VARCHAR(64),
  createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completedAt TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_idemp_key ON idempotency_keys(idempotencyKey);

-- Passengers table
CREATE TABLE IF NOT EXISTS passengers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  age INT NOT NULL,
  gender VARCHAR(20) NOT NULL,
  seat_number VARCHAR(20) NOT NULL,
  id_proof_type VARCHAR(50) NOT NULL,
  id_proof_number VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  train_id BIGINT NOT NULL,
  pnr_number VARCHAR(20) NOT NULL UNIQUE,
  booking_time TIMESTAMP NOT NULL,
  status VARCHAR(50) NOT NULL,
  total_fare DECIMAL(10, 2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  booking_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_bookings_pnr ON bookings(pnr_number);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_train ON bookings(train_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_passenger ON passengers(id);

-- Foreign key for passengers to bookings
ALTER TABLE passengers ADD COLUMN IF NOT EXISTS booking_id BIGINT;
ALTER TABLE passengers ADD CONSTRAINT IF NOT EXISTS fk_passenger_booking 
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

