## Mobile Wallet & UPI Integration

### Components
- **Database** (`V9__Create_mobile_wallet_tables.sql`)
  - `upi_payment_intents` tracks UPI order lifecycle.
  - `mobile_wallet_transactions` stores third-party wallet transactions.
- **Entities & Repos**
  - `UpiPaymentIntent`, `MobileWalletTransaction` plus repositories.
- **Services**
  - `PaymentQrCodeService`: Generates QR payloads (base64) for display/scanning.
  - `MobileWalletIntegrationService`: Initiates UPI flows, processes wallet tokens, and produces QR codes.
- **APIs** (`MobileWalletController`)
  - `POST /api/payments/upi`
  - `POST /api/payments/wallet`
  - `GET /api/payments/qr-code`
- **DTOs**
  - `UpiPaymentRequest/Response`, `WalletPaymentRequest/Response`, `QrCodeResponse`.
- **Tests**
  - Service + controller unit tests cover UPI + wallet scenarios.

### Behaviour
1. Client posts UPI order → intent persisted → QR payload generated → status flips to `COMPLETED` with simulated UTR.
2. Wallet payments store token metadata (last four) and mark transaction `COMPLETED`.
3. QR endpoint can be used for quick static QR issuance for counter payments.

