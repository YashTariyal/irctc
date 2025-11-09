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
  idempotency_key VARCHAR(128) NOT NULL UNIQUE,
  http_method VARCHAR(16) NOT NULL,
  request_path VARCHAR(255) NOT NULL,
  request_hash TEXT,
  response_body TEXT,
  response_status VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_idemp_key ON idempotency_keys(idempotency_key);

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

-- Audit Logs table
CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  entity_type VARCHAR(100) NOT NULL,
  entity_id BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL,
  user_id VARCHAR(255),
  username VARCHAR(255),
  ip_address VARCHAR(50),
  http_method VARCHAR(10),
  request_path VARCHAR(500),
  request_body TEXT,
  response_body TEXT,
  response_status INT,
  old_values TEXT,
  new_values TEXT,
  error_message TEXT,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  additional_info VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);

