# Offline Mode Implementation

This document describes the server-side capabilities that were added to enable a first-class offline experience across the IRCTC platform.

---

## 🎯 Objectives

1. Provide mobile/web clients with a compact data bundle that can be cached and used while offline.
2. Allow user actions that were executed without connectivity to be replayed safely when the network returns.
3. Queue payment attempts that were initiated offline and process them through the normal payment gateways when online again.

---

## 🚉 Booking Service (`irctc-booking-service`)

### New APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/offline/sync` | Generates an offline bundle (tickets, schedules, pending actions) tailored to the user/device request. |
| `GET` | `/api/offline/users/{userId}/tickets` | Lightweight endpoint for quickly refreshing cached tickets. |
| `POST` | `/api/offline/actions` | Queues an action performed offline (e.g., check-in, modification). |
| `GET` | `/api/offline/actions/pending?userId=` | Lists queued/processing actions for a user. |
| `POST` | `/api/offline/actions/process` | Server-side replay of pending actions (supports optional `userId` filter). |

### Key Components

- **`OfflineSyncService`**: Aggregates bookings, passengers, cached train schedules and pending offline actions into a single payload.
- **`OfflineActionService` & `OfflineAction` entity**: Persist offline actions (QUEUED → PROCESSING → COMPLETED/FAILED) with timestamps for auditability.
- **Schedule Cache**: A lightweight in-memory cache (30-minute TTL) that stores train schedule snapshots to minimise repeated calls to the Train Service.
- **Flyway Migration**: `V13__Create_offline_actions_table.sql` introduces the `offline_actions` table with indexes on `user_id`, `status`, and `tenant_id`.

### DTO Highlights

- `OfflineTicketDTO`, `OfflinePassengerDTO`, `OfflineTrainScheduleDTO`
- `OfflineSyncRequest` (user preferences, last sync timestamp, train filters)
- `OfflineSyncResponse` (tickets, schedules, pending actions + metadata)

---

## 💳 Payment Service (`irctc-payment-service`)

### New APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/payments/offline` | Queue a payment attempt that was captured offline. |
| `GET` | `/api/payments/offline/user/{userId}` | List queued/processing offline payments for a user. |
| `POST` | `/api/payments/offline/{intentId}/sync` | Force a sync for a single offline payment intent. |
| `POST` | `/api/payments/offline/sync` | Process a batch of queued intents (also triggered via scheduler). |

### Key Components

- **`OfflinePaymentQueueService`**:
  - Persists `OfflinePaymentIntent` entities.
  - Rebuilds `SimplePayment` objects and calls the existing `SimplePaymentService` for actual gateway processing.
  - Provides a scheduled sync (`@Scheduled(fixedDelay = 5 minutes)`) ensuring offline payments are retried automatically.
- **`OfflinePaymentIntent` Entity**:
  - Fields for `userId`, `bookingId`, `amount`, `paymentMethod`, `status`, `failureReason`, `processedPaymentId`, timestamps.
  - Migration `V7__Create_offline_payment_intents_table.sql`.
- **DTOs**: `OfflinePaymentRequest` and `OfflinePaymentResponse`.

---

## ✅ Test Coverage

- Added unit tests for `OfflineSyncService`, `OfflineActionService`, `OfflinePaymentQueueService`.
- Added controller tests for `OfflineSyncController` and `OfflinePaymentController`.
- Existing suites (`mvn test`) for both services are green.

---

## 🧭 Client Integration Notes

1. **Initial sync flow**:
   - POST `/api/offline/sync` with `userId`, `lastSyncTime`, preferred routes/train numbers.
   - Cache tickets + schedules locally.
2. **Offline actions**:
   - Queue actions locally.
   - POST `/api/offline/actions` when the device reconnects (batching supported client-side).
3. **Offline payments**:
   - Queue payment intent via `/api/payments/offline`.
   - Poll `/api/payments/offline/user/{userId}` to show pending/completed states.
   - Use `/api/payments/offline/sync` to force processing immediately after connectivity is restored (optional if relying on scheduler).

---

## 📌 Future Enhancements

- Tie offline actions to specific processors (e.g., automatically triggering seat upgrades/check-ins instead of marking as completed).
- Provide delta compression / ETag-based sync for large ticket bundles.
- Surface offline payment analytics in the payment dashboard.

---

**Offline Mode is now fully wired into Booking & Payment services, giving clients the primitives they need to deliver a resilient offline-first experience.**

