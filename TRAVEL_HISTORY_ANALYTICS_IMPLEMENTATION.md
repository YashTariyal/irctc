## Travel History & Analytics

### Goals
- Give passengers insight into trip counts, spend patterns, and favorite routes.
- Provide a chronological travel timeline and allow exporting booking history.

### Booking Service changes
1. **DTOs**
   - `TravelAnalyticsResponse` with nested `MonthlySummary`, `RouteSummary`, `TrainSummary`.
   - `TravelTimelineEntry` and `TravelExportResponse`.
2. **Service**
   - `TravelAnalyticsService` aggregates `SimpleBooking` data, enriches with train info via `TrainServiceClient`, and produces:
     - Summary stats (trips, spend, distance, unique routes/trains, monthly breakdown).
     - Favorite routes & top trains lists.
     - Timeline entries sorted by booking time.
     - Base64-encoded CSV export (`travel-history-{user}.csv`).
   - Tenant-aware filtering ensures multi-tenant isolation.
3. **Controller**
   - `TravelAnalyticsController` exposes:
     - `GET /api/bookings/user/{id}/analytics`
     - `GET /api/bookings/user/{id}/favorite-routes`
     - `GET /api/bookings/user/{id}/travel-timeline`
     - `GET /api/bookings/user/{id}/export?format=csv`
4. **Train Client**
   - `TrainServiceClient.TrainResponse` now surfaces `distance` and `duration` for analytics.

### Testing
- `TravelAnalyticsServiceTest` exercises aggregation, timeline ordering, and export.
- `TravelAnalyticsControllerTest` covers all endpoints with mocked service responses.

