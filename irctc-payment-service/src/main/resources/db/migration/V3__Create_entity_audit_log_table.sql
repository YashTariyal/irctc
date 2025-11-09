-- Entity Audit Log table for tracking all entity changes
CREATE TABLE IF NOT EXISTS entity_audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  entity_name VARCHAR(100) NOT NULL,
  entity_id BIGINT NOT NULL,
  revision_number BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL,
  changed_by VARCHAR(255),
  changed_by_username VARCHAR(255),
  ip_address VARCHAR(50),
  changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  old_values TEXT,
  new_values TEXT,
  changed_fields TEXT,
  metadata TEXT,
  CONSTRAINT chk_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
);

CREATE INDEX IF NOT EXISTS idx_audit_entity_name_id ON entity_audit_log(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_revision ON entity_audit_log(entity_name, entity_id, revision_number);
CREATE INDEX IF NOT EXISTS idx_audit_changed_by ON entity_audit_log(changed_by);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON entity_audit_log(changed_at);
CREATE INDEX IF NOT EXISTS idx_audit_action ON entity_audit_log(action);

