-- QR Code Ticket Verification Schema Migration

-- QR Codes table
CREATE TABLE IF NOT EXISTS qr_codes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  qr_code VARCHAR(500) NOT NULL UNIQUE,
  pnr_number VARCHAR(100) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  verified_at TIMESTAMP,
  verified_by VARCHAR(100),
  tenant_id VARCHAR(50),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_qr_codes_booking_id ON qr_codes(booking_id);
CREATE INDEX IF NOT EXISTS idx_qr_codes_code ON qr_codes(qr_code);
CREATE INDEX IF NOT EXISTS idx_qr_codes_tenant_id ON qr_codes(tenant_id);
CREATE INDEX IF NOT EXISTS idx_qr_codes_pnr ON qr_codes(pnr_number);

