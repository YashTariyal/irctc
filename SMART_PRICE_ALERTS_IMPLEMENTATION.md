## Smart Price Alerts Implementation

### Goals
- Let users subscribe to price drop or seat availability alerts
- Continuously evaluate train data and trigger notifications when thresholds are met
- Provide CRUD APIs to manage alerts

### Train Service Changes
1. **Database**
   - Added `price_alerts` table via `V5__Create_price_alerts_table.sql`
   - Fields track user/contact info, route, threshold, notification channel, status, and metadata
2. **Domain & Repos**
   - New `PriceAlert` entity implements `TenantAware`
   - `PriceAlertRepository` offers lookups by user and status
3. **APIs**
   - `PriceAlertController` (`/api/trains/price-alerts`) exposes:
     - `POST /` create alert
     - `GET /{id}` fetch alert
     - `GET /user/{userId}` list alerts
     - `PUT /{id}` update
     - `DELETE /{id}` remove
4. **Services**
   - `PriceAlertService` handles validation + persistence
   - `PriceAlertEvaluationService` runs on a scheduled loop to evaluate active alerts
   - Evaluation checks current train fares/availability via `SimpleTrainRepository`
5. **Notifications**
   - Added Feign `NotificationServiceClient` to call the notification service `/api/notifications/push`
   - When an alert condition is met, a push notification is sent with alert metadata
6. **Scheduling**
   - Enabled Spring scheduling in `TrainServiceApplication`
   - Interval configurable via `price.alerts.evaluation.delay` property

### Configuration
- `price.alerts.notification.enabled` (default `true`) toggles outbound notifications
- `price.alerts.evaluation.delay` controls scheduler frequency (default 60s)

### Testing
- Controller + service unit tests cover CRUD flows and predicate evaluation (see `PriceAlertControllerTest`, `PriceAlertServiceTest`, `PriceAlertEvaluationServiceTest`).

