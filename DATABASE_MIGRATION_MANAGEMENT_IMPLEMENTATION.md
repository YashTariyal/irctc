# Database Migration Management Implementation Guide

## Overview

Comprehensive database migration management system using Flyway with status tracking, history management, validation, and health monitoring.

## Features

### 1. **Migration Status Tracking**
- ✅ Total migrations count
- ✅ Applied migrations
- ✅ Pending migrations
- ✅ Failed migrations
- ✅ Current version tracking

### 2. **Migration History**
- ✅ Complete migration history
- ✅ Migration details (version, description, state, type)
- ✅ Installation information (date, user, execution time)
- ✅ Script and checksum information

### 3. **Migration Validation**
- ✅ Automatic validation on startup
- ✅ Manual validation endpoint
- ✅ Checksum verification
- ✅ Version consistency checks

### 4. **Migration Health Checks**
- ✅ Health indicator for migrations
- ✅ Failed migration detection
- ✅ Pending migration warnings
- ✅ Integration with Spring Boot Actuator

### 5. **Migration Management API**
- ✅ Status summary endpoint
- ✅ Migration history endpoint
- ✅ Migration details endpoint
- ✅ Validation endpoint

## Architecture

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Service   │───▶│  Migration       │───▶│     Flyway      │
│   Startup   │    │  Management      │    │   (Database)    │
└─────────────┘    └──────────────────┘    └─────────────────┘
      │                    │                         │
      │                    ▼                         │
      │            ┌─────────────────┐              │
      │            │  Migration      │              │
      │            │  History        │              │
      │            └─────────────────┘              │
      │                                              │
      └──────────────────────────────────────────────┘
              REST API & Health Checks
```

## Migration Status

### Status Summary

**API Endpoint:**
```http
GET /api/migrations/status
```

**Response:**
```json
{
  "totalMigrations": 6,
  "appliedMigrations": 6,
  "pendingMigrations": 0,
  "failedMigrations": 0,
  "currentVersion": "6",
  "timestamp": "2024-12-28T10:30:00"
}
```

### Migration States

- **SUCCESS**: Migration applied successfully
- **PENDING**: Migration not yet applied
- **FAILED**: Migration failed during execution
- **OUT_OF_ORDER**: Migration applied out of order
- **MISSING**: Expected migration is missing
- **FUTURE**: Migration version is in the future

## Migration History

### Get Migration History

**API Endpoint:**
```http
GET /api/migrations/history
```

**Response:**
```json
[
  {
    "version": "1",
    "description": "Initial booking schema",
    "type": "SQL",
    "state": "SUCCESS",
    "installedOn": "2024-01-01T10:00:00",
    "installedBy": "sa",
    "executionTime": 150,
    "script": "V1__Initial_booking_schema.sql"
  },
  {
    "version": "2",
    "description": "Create saga instances table",
    "type": "SQL",
    "state": "SUCCESS",
    "installedOn": "2024-01-15T10:00:00",
    "installedBy": "sa",
    "executionTime": 50,
    "script": "V2__Create_saga_instances_table.sql"
  }
]
```

## Migration Details

### Get Specific Migration

**API Endpoint:**
```http
GET /api/migrations/{version}
```

**Example:**
```http
GET /api/migrations/1
```

**Response:**
```json
{
  "version": "1",
  "description": "Initial booking schema",
  "state": "SUCCESS",
  "type": "SQL",
  "installedOn": "2024-01-01T10:00:00",
  "installedBy": "sa",
  "installedRank": 1,
  "executionTime": 150,
  "script": "V1__Initial_booking_schema.sql",
  "checksum": 1234567890
}
```

## Migration Validation

### Validate Migrations

**API Endpoint:**
```http
POST /api/migrations/validate
```

**Response (Success):**
```json
{
  "valid": true,
  "message": "All migrations are valid",
  "timestamp": "2024-12-28T10:30:00"
}
```

**Response (Failure):**
```json
{
  "valid": false,
  "message": "Validation failed: Migration checksum mismatch",
  "timestamp": "2024-12-28T10:30:00"
}
```

## Migration Health Check

### Health Indicator

**Actuator Endpoint:**
```http
GET /actuator/health/migration
```

**Response (Healthy):**
```json
{
  "status": "UP",
  "details": {
    "status": "All migrations applied successfully",
    "totalMigrations": 6,
    "appliedMigrations": 6,
    "pendingMigrations": 0,
    "currentVersion": "6"
  }
}
```

**Response (Unhealthy):**
```json
{
  "status": "DOWN",
  "details": {
    "status": "Migrations have failures",
    "failedCount": 1,
    "currentVersion": "5"
  }
}
```

## API Endpoints

### Get All Migrations

```http
GET /api/migrations
```

**Response:**
```json
{
  "all": [...],
  "applied": [...],
  "pending": [...],
  "failed": [...],
  "currentVersion": "6"
}
```

### Get Applied Migrations

```http
GET /api/migrations/applied
```

### Get Pending Migrations

```http
GET /api/migrations/pending
```

### Get Failed Migrations

```http
GET /api/migrations/failed
```

### Get Current Version

```http
GET /api/migrations/current
```

**Response:**
```json
{
  "version": "6"
}
```

## Flyway Configuration

### Application Properties

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true  # Prevent accidental cleanup
    out-of-order: false  # Require sequential migrations
    ignore-missing-migrations: false
    ignore-ignored-migrations: false
    ignore-pending-migrations: false
```

### Actuator Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,flyway
  endpoint:
    flyway:
      enabled: true
```

## Migration Naming Convention

### Standard Format

```
V{version}__{description}.sql
```

**Examples:**
- `V1__Initial_booking_schema.sql`
- `V2__Create_saga_instances_table.sql`
- `V3__Create_booking_events_table.sql`
- `V4__Create_event_tracking_tables.sql`
- `V5__Alter_audit_logs_entity_id_nullable.sql`
- `V6__Create_entity_audit_log_table.sql`

### Version Numbering

- Use sequential numbers: 1, 2, 3, ...
- Use descriptive names with underscores
- Keep descriptions concise but clear

## Migration Best Practices

### 1. **Idempotent Migrations**
- Use `IF NOT EXISTS` for tables
- Use `IF EXISTS` for drops
- Handle existing data gracefully

### 2. **Backward Compatibility**
- Avoid breaking changes when possible
- Use additive migrations
- Document breaking changes

### 3. **Testing**
- Test migrations in development first
- Test rollback procedures
- Validate on staging before production

### 4. **Version Control**
- Commit migrations with code
- Review migrations in PRs
- Document migration purpose

### 5. **Rollback Strategy**
- Create rollback scripts when needed
- Test rollback procedures
- Document rollback steps

## Migration Workflow

### 1. Create Migration

```sql
-- V7__Add_booking_notes_column.sql
ALTER TABLE bookings 
ADD COLUMN notes VARCHAR(500);
```

### 2. Test Migration

```bash
# Start service - migration runs automatically
./mvnw spring-boot:run

# Check migration status
curl http://localhost:8093/api/migrations/status
```

### 3. Validate Migration

```bash
# Validate migrations
curl -X POST http://localhost:8093/api/migrations/validate
```

### 4. Check Health

```bash
# Check migration health
curl http://localhost:8093/actuator/health/migration
```

## Monitoring

### Prometheus Metrics

Flyway provides metrics via Spring Boot Actuator:
```
flyway_migrations_total - Total number of migrations
flyway_migrations_applied_total - Number of applied migrations
flyway_migrations_pending_total - Number of pending migrations
```

### Grafana Dashboard

**Recommended Panels:**
1. Migration status over time
2. Migration execution time
3. Failed migrations count
4. Current migration version
5. Pending migrations alert

## Troubleshooting

### Migration Failed

**Symptoms:**
- Service fails to start
- Migration status shows FAILED
- Health check returns DOWN

**Solutions:**
1. Check migration logs
2. Fix SQL syntax errors
3. Resolve data conflicts
4. Repair migration if needed

### Checksum Mismatch

**Symptoms:**
- Validation fails
- Checksum error in logs

**Solutions:**
1. Verify migration file hasn't changed
2. Check for manual database changes
3. Repair checksum if intentional change

### Pending Migrations

**Symptoms:**
- Pending migrations count > 0
- Service starts but migrations not applied

**Solutions:**
1. Check Flyway configuration
2. Verify migration files exist
3. Check database permissions
4. Review migration order

## Files Created

### Core Components
- `MigrationManagementService.java` - Migration management service
- `MigrationManagementController.java` - REST API
- `MigrationHealthIndicator.java` - Health check

### Configuration
- `application.yml` - Flyway and actuator configuration

## Benefits

1. **Visibility**
   - Clear migration status
   - Complete migration history
   - Real-time monitoring

2. **Reliability**
   - Validation before deployment
   - Health checks
   - Failure detection

3. **Management**
   - Migration tracking
   - Version control
   - Rollback support

4. **Compliance**
   - Audit trail
   - Migration history
   - Change tracking

5. **Debugging**
   - Migration details
   - Execution time tracking
   - Error information

## Conclusion

The Database Migration Management implementation provides comprehensive migration tracking, validation, and monitoring capabilities. This enables safe and reliable database schema evolution across all microservices.

