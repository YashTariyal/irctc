-- Booking Events table for Event Sourcing
CREATE TABLE IF NOT EXISTS booking_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL UNIQUE,
  aggregate_id VARCHAR(50) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  event_data TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  correlation_id VARCHAR(100),
  user_id VARCHAR(100),
  version VARCHAR(50) DEFAULT '1.0',
  event_metadata TEXT
);

CREATE INDEX IF NOT EXISTS idx_booking_events_aggregate ON booking_events(aggregate_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_booking_events_type ON booking_events(event_type, timestamp);
CREATE INDEX IF NOT EXISTS idx_booking_events_correlation ON booking_events(correlation_id);

