-- Create data_export_requests table for GDPR data export
CREATE TABLE IF NOT EXISTS data_export_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    file_url VARCHAR(500),
    expires_at TIMESTAMP,
    requested_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    data_categories VARCHAR(500),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_export_requests_user_id (user_id),
    INDEX idx_export_requests_status (status),
    INDEX idx_export_requests_tenant_id (tenant_id)
);

