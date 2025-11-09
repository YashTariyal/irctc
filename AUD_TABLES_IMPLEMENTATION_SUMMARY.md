# AUD Tables Implementation Summary

## Overview
Implemented comprehensive AUD (Audit) tables for transactional management across microservices. This provides automatic tracking of all CREATE, UPDATE, and DELETE operations on entities.

## Implementation Status

### ✅ Booking Service - COMPLETE

#### Files Created:
1. **EntityAuditLog.java** - Generic audit entity
   - Tracks: entityName, entityId, revisionNumber, action, changedBy, changedAt, oldValues, newValues
   - Location: `irctc-booking-service/src/main/java/com/irctc/booking/audit/entity/`

2. **EntityAuditLogRepository.java** - Repository with query methods
   - Methods: findByEntityNameAndEntityId, getNextRevisionNumber, findByAction, etc.
   - Location: `irctc-booking-service/src/main/java/com/irctc/booking/audit/repository/`

3. **EntityAuditListener.java** - JPA Entity Listener
   - Automatically tracks CREATE, UPDATE, DELETE operations
   - Uses ApplicationContextAware to access Spring beans
   - Location: `irctc-booking-service/src/main/java/com/irctc/booking/audit/`

4. **EntityAuditService.java** - Service layer
   - Provides methods to query audit history
   - Location: `irctc-booking-service/src/main/java/com/irctc/booking/audit/service/`

5. **EntityAuditController.java** - REST API
   - Endpoints for querying audit logs
   - Location: `irctc-booking-service/src/main/java/com/irctc/booking/audit/controller/`

6. **V6__Create_entity_audit_log_table.sql** - Database migration
   - Creates `entity_audit_log` table
   - Location: `irctc-booking-service/src/main/resources/db/migration/`

#### Entity Integration:
- ✅ `SimpleBooking` - Added `@EntityListeners(EntityAuditListener.class)`
- ✅ `SimplePassenger` - Added `@EntityListeners(EntityAuditListener.class)`

### Features Implemented:

1. **Automatic Audit Tracking**
   - CREATE operations: Logs new entity state
   - UPDATE operations: Logs old and new values
   - DELETE operations: Logs entity state before deletion

2. **Revision Numbers**
   - Sequential revision numbers per entity
   - Tracks complete change history

3. **User Tracking**
   - Captures user ID from `X-User-Id` header
   - Captures username from `X-Username` header
   - Falls back to "SYSTEM" if not available

4. **IP Address Tracking**
   - Captures client IP address
   - Supports X-Forwarded-For and X-Real-IP headers

5. **REST API Endpoints**
   - `GET /api/audit/entity/{entityName}/{entityId}` - Get complete audit history
   - `GET /api/audit/entity/{entityName}/{entityId}/latest` - Get latest audit log
   - `GET /api/audit/entity/{entityName}/{entityId}/action/{action}` - Get logs by action
   - `GET /api/audit/user/{userId}` - Get logs by user
   - `GET /api/audit/time-range?start=...&end=...` - Get logs by time range
   - `GET /api/audit/stats/{entityName}/{entityId}` - Get audit statistics

## Testing

### Prerequisites:
1. Booking Service must be running
2. Service must be restarted after code changes to pick up new EntityAuditListener
3. Database migration V6 must have run (creates `entity_audit_log` table)

### Test Script:
- `test-audit-tables.sh` - Comprehensive test script
- Tests CREATE, UPDATE, DELETE operations
- Verifies audit logs are created and queryable

### Manual Testing Steps:

1. **Create a Booking:**
   ```bash
   curl -X POST http://localhost:8093/api/bookings \
     -H "Content-Type: application/json" \
     -H "X-User-Id: test-user-123" \
     -H "X-Username: testuser" \
     -d '{
       "userId": 1,
       "trainId": 1,
       "pnrNumber": "TEST_PNR_001",
       "bookingTime": "2025-12-01T10:00:00",
       "status": "CONFIRMED",
       "totalFare": 500.00
     }'
   ```

2. **Check Audit Log:**
   ```bash
   curl http://localhost:8093/api/audit/entity/SimpleBooking/{bookingId}
   ```

3. **Update Booking:**
   ```bash
   curl -X PUT http://localhost:8093/api/bookings/{bookingId} \
     -H "Content-Type: application/json" \
     -H "X-User-Id: test-user-456" \
     -H "X-Username: updater" \
     -d '{...updated booking data...}'
   ```

4. **Check Updated Audit Log:**
   ```bash
   curl http://localhost:8093/api/audit/entity/SimpleBooking/{bookingId}
   ```

## Next Steps

### For Payment Service:
- Create similar AUD table infrastructure
- Add `@EntityListeners` to `SimplePayment` entity
- Create database migration

### For User Service:
- Create similar AUD table infrastructure
- Add `@EntityListeners` to `SimpleUser` and `NotificationPreferences` entities
- Create database migration

## Technical Notes

1. **JPA Entity Listeners:**
   - Cannot directly inject Spring beans
   - Uses `ApplicationContextAware` pattern to access Spring context
   - Repository is retrieved from ApplicationContext when needed

2. **Revision Numbers:**
   - Calculated using `MAX(revisionNumber) + 1` query
   - Ensures sequential numbering per entity

3. **Old Values for UPDATE:**
   - Retrieved from the last audit log's `newValues` field
   - This provides a complete change history

4. **Transaction Management:**
   - Audit logs are saved in the same transaction
   - For production, consider using `@Async` or message queue for better performance

## Database Schema

```sql
CREATE TABLE entity_audit_log (
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
  metadata TEXT
);
```

## Status

✅ **Booking Service**: Implementation complete, ready for testing
⏳ **Payment Service**: Pending
⏳ **User Service**: Pending
⏳ **Train Service**: Not needed (read-only service)

