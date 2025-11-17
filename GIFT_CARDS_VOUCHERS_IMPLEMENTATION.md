## Gift Cards & Vouchers

### Database
- `V10__Create_gift_cards_and_vouchers.sql` introduces:
  - `gift_cards` + `gift_card_transactions`
  - `vouchers`

### Domain
- Entities: `GiftCard`, `GiftCardTransaction`, `Voucher`.
- DTOs: purchase/redeem/balance/validation payloads.
- Repositories for each.

### Services
- `GiftCardService`
  - Purchase: min ₹100, unique code (`GC-XXXX`), optional expiry.
  - Redeem: balance deduction, auto `CONSUMED` when zero.
  - Balance lookup + audit transactions.
- `VoucherService`
  - Supports flat or percentage discounts, min order amount, usage-limits, expiry validation.

### APIs (`GiftCardController`)
- `POST /api/gift-cards/purchase`
- `POST /api/gift-cards/redeem`
- `GET /api/gift-cards/{code}/balance`
- `GET /api/vouchers/validate?code=&orderAmount=`

### Tests
- Unit tests for services (purchase/redeem & voucher validation).
- Controller tests exercising all endpoints.

