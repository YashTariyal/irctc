-- Event Consumption Log table for tracking event consumption with idempotency
CREATE TABLE IF NOT EXISTS event_consumption_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL UNIQUE,
  service_name VARCHAR(100) NOT NULL,
  topic VARCHAR(100) NOT NULL,
  partition_number INT NOT NULL,
  "offset" BIGINT NOT NULL,
  consumer_group VARCHAR(100) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  payload TEXT NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
  retry_count INT NOT NULL DEFAULT 0,
  max_retries INT NOT NULL DEFAULT 3,
  processing_time_ms BIGINT,
  error_message TEXT,
  error_stack_trace TEXT,
  correlation_id VARCHAR(100),
  received_at TIMESTAMP NOT NULL,
  processed_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  metadata TEXT,
  CONSTRAINT chk_cons_status CHECK (status IN ('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_cons_status_created ON event_consumption_log(status, created_at);
CREATE INDEX IF NOT EXISTS idx_cons_topic_partition_offset ON event_consumption_log(topic, partition_number, "offset");
CREATE INDEX IF NOT EXISTS idx_cons_consumer_group ON event_consumption_log(consumer_group, status);
CREATE INDEX IF NOT EXISTS idx_cons_correlation ON event_consumption_log(correlation_id);
CREATE INDEX IF NOT EXISTS idx_cons_event_id ON event_consumption_log(event_id);

