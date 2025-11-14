-- Notification Enhancements Migration
-- Adds tables for WhatsApp templates, device tokens, preferences, and scheduled notifications

-- WhatsApp Templates Table
CREATE TABLE IF NOT EXISTS whatsapp_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    language VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    approved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_templates_tenant_id ON whatsapp_templates(tenant_id);
CREATE INDEX IF NOT EXISTS idx_whatsapp_templates_status ON whatsapp_templates(status);

-- User Device Tokens Table (for push notifications)
CREATE TABLE IF NOT EXISTS user_device_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL,
    device_id VARCHAR(200),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON user_device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_token ON user_device_tokens(token);
CREATE INDEX IF NOT EXISTS idx_device_tokens_tenant_id ON user_device_tokens(tenant_id);

-- Notification Preferences Table
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    booking_confirmed BOOLEAN NOT NULL DEFAULT TRUE,
    payment_success BOOLEAN NOT NULL DEFAULT TRUE,
    booking_reminder BOOLEAN NOT NULL DEFAULT TRUE,
    cancellation BOOLEAN NOT NULL DEFAULT TRUE,
    modification BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    digest_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    digest_frequency VARCHAR(20),
    channel_preferences TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_prefs_user_id ON notification_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_prefs_tenant_id ON notification_preferences(tenant_id);

-- Scheduled Notifications Table
CREATE TABLE IF NOT EXISTS scheduled_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    recipient VARCHAR(500) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    template_id VARCHAR(100),
    template_variables TEXT,
    scheduled_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(20),
    sent_time TIMESTAMP,
    error_message TEXT,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_scheduled_notifications_user_id ON scheduled_notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_notifications_status_time ON scheduled_notifications(status, scheduled_time);
CREATE INDEX IF NOT EXISTS idx_scheduled_notifications_tenant_id ON scheduled_notifications(tenant_id);

