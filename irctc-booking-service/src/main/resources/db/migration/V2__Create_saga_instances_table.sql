-- Saga Instances table for distributed transaction management
CREATE TABLE IF NOT EXISTS saga_instances (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  saga_id VARCHAR(100) NOT NULL UNIQUE,
  saga_type VARCHAR(50) NOT NULL,
  correlation_id VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL,
  current_step INT NOT NULL DEFAULT 0,
  total_steps INT NOT NULL,
  saga_data TEXT,
  compensation_data TEXT,
  error_message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  completed_at TIMESTAMP,
  CONSTRAINT chk_saga_status CHECK (status IN ('STARTED', 'IN_PROGRESS', 'COMPLETED', 'COMPENSATING', 'COMPENSATED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_saga_status_created ON saga_instances(status, created_at);
CREATE INDEX IF NOT EXISTS idx_saga_correlation ON saga_instances(correlation_id);

