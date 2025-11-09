# Multi-Tenancy Support Implementation Guide

## Overview

Comprehensive multi-tenancy support for IRCTC microservices using the **Shared Database, Shared Schema** approach. This implementation provides tenant isolation, tenant context propagation, and tenant management capabilities.

## Architecture

### Approach: Shared Database, Shared Schema

- **Single Database**: All tenants share the same database
- **Shared Schema**: All tenants use the same table structure
- **Tenant Isolation**: Data is isolated using `tenant_id` column
- **Row-Level Filtering**: Hibernate filters automatically filter data by tenant

### Benefits

- ✅ Cost-effective (single database)
- ✅ Easy to maintain
- ✅ Simple backup/restore
- ✅ Good performance with proper indexing
- ✅ Easy to scale

## Components

### 1. Tenant Entity

**Location**: `Tenant.java`

Represents a tenant in the system:
- `code`: Unique tenant identifier (e.g., "acme-corp")
- `name`: Tenant display name
- `status`: ACTIVE, SUSPENDED, INACTIVE
- `configuration`: JSON configuration for tenant-specific settings

### 2. Tenant Context

**Location**: `TenantContext.java`

Thread-local storage for current tenant:
- `setTenantId(String)`: Set current tenant ID
- `getTenantId()`: Get current tenant ID
- `setTenantCode(String)`: Set current tenant code
- `getTenantCode()`: Get current tenant code
- `clear()`: Clear tenant context

### 3. Tenant Resolver

**Location**: `TenantResolver.java`

HTTP interceptor that extracts tenant information from:
1. **X-Tenant-Id Header**: Direct tenant ID
2. **X-Tenant-Code Header**: Tenant code (resolved to ID)
3. **Subdomain**: Extracts tenant from subdomain (e.g., `tenant1.example.com`)
4. **JWT Claims**: (Future) Extract from JWT token

### 4. Tenant-Aware Interface

**Location**: `TenantAware.java`

Marker interface for entities that support multi-tenancy:
```java
public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}
```

### 5. Hibernate Tenant Filter

**Location**: `TenantFilter.java`

Automatic row-level filtering using Hibernate filters:
- Filters data by `tenant_id` automatically
- Applied to all queries for tenant-aware entities

## Database Schema

### Tenants Table

```sql
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    configuration TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
```

### Tenant ID Column

All tenant-aware entities have a `tenant_id` column:
- `bookings.tenant_id`
- `passengers.tenant_id`
- (Add to other entities as needed)

## Usage

### 1. Creating a Tenant

```http
POST /api/tenants
Content-Type: application/json

{
  "code": "acme-corp",
  "name": "ACME Corporation",
  "email": "admin@acme.com",
  "phone": "+1234567890",
  "status": "ACTIVE"
}
```

### 2. Making Requests with Tenant Context

**Option 1: Using X-Tenant-Id Header**
```http
GET /api/bookings
X-Tenant-Id: 1
```

**Option 2: Using X-Tenant-Code Header**
```http
GET /api/bookings
X-Tenant-Code: acme-corp
```

**Option 3: Using Subdomain**
```http
GET http://acme-corp.example.com/api/bookings
```

### 3. Creating a Booking (Tenant-Aware)

```http
POST /api/bookings
X-Tenant-Id: 1
Content-Type: application/json

{
  "userId": 1,
  "trainId": 1,
  "totalFare": 500.00,
  "passengers": [...]
}
```

The booking will automatically have `tenant_id` set to `1`.

### 4. Querying Bookings

```http
GET /api/bookings
X-Tenant-Id: 1
```

Only bookings with `tenant_id = 1` will be returned.

## API Endpoints

### Tenant Management

- `POST /api/tenants` - Create tenant
- `GET /api/tenants` - Get all tenants
- `GET /api/tenants/{id}` - Get tenant by ID
- `GET /api/tenants/code/{code}` - Get tenant by code
- `PUT /api/tenants/{id}` - Update tenant
- `DELETE /api/tenants/{id}` - Delete tenant
- `POST /api/tenants/{id}/activate` - Activate tenant
- `POST /api/tenants/{id}/suspend` - Suspend tenant

## Security

### Tenant Isolation

1. **Automatic Filtering**: Hibernate filters automatically filter queries
2. **Service-Level Validation**: Services validate tenant access
3. **Context Validation**: TenantResolver validates tenant exists and is active

### Access Control

- Users can only access data belonging to their tenant
- Cross-tenant access attempts are logged and blocked
- Tenant context is required for all protected endpoints

## Configuration

### Application Properties

```yaml
multi-tenancy:
  enabled: true  # Enable multi-tenancy support
  required: true  # Require tenant context for all requests
  header-tenant-id: "X-Tenant-Id"
  header-tenant-code: "X-Tenant-Code"
  subdomain-extraction: true
```

## Implementation Details

### Entity Updates

All tenant-aware entities:
1. Implement `TenantAware` interface
2. Add `tenant_id` column
3. Add Hibernate filter annotations
4. Add index on `tenant_id`

### Service Updates

Services:
1. Set `tenant_id` from context when creating entities
2. Validate tenant access when reading entities
3. Filter queries by tenant

### Repository Updates

Repositories:
1. Use Hibernate filters for automatic filtering
2. Add tenant-specific query methods if needed

## Migration Strategy

### Existing Data

For existing data without `tenant_id`:
1. Create a default tenant
2. Update existing records with default tenant ID
3. Or mark records as "legacy" and handle separately

### Migration Script

```sql
-- Add tenant_id column
ALTER TABLE bookings ADD COLUMN tenant_id VARCHAR(50);

-- Create default tenant
INSERT INTO tenants (code, name, status) VALUES ('default', 'Default Tenant', 'ACTIVE');

-- Update existing records (if needed)
UPDATE bookings SET tenant_id = (SELECT id FROM tenants WHERE code = 'default');
```

## Testing

### Unit Tests

```java
@Test
public void testTenantIsolation() {
    // Set tenant context
    TenantContext.setTenantId("1");
    
    // Create booking
    SimpleBooking booking = new SimpleBooking();
    bookingService.createBooking(booking);
    
    // Verify tenant_id is set
    assertEquals("1", booking.getTenantId());
    
    // Clear context
    TenantContext.clear();
}
```

### Integration Tests

```java
@Test
public void testTenantAccessControl() {
    // Create booking for tenant 1
    TenantContext.setTenantId("1");
    SimpleBooking booking = bookingService.createBooking(new SimpleBooking());
    
    // Try to access as tenant 2
    TenantContext.setTenantId("2");
    Optional<SimpleBooking> result = bookingService.getBookingById(booking.getId());
    
    // Should return empty
    assertFalse(result.isPresent());
}
```

## Best Practices

### 1. Always Set Tenant Context

- Set tenant context early in request processing
- Validate tenant exists and is active
- Clear context after request completion

### 2. Validate Tenant Access

- Always validate tenant access in services
- Log access violations
- Return appropriate error messages

### 3. Index Tenant ID

- Add indexes on `tenant_id` columns
- Improves query performance
- Essential for large datasets

### 4. Tenant Configuration

- Use `configuration` JSON field for tenant-specific settings
- Support feature flags per tenant
- Customize behavior per tenant

### 5. Monitoring

- Monitor tenant usage
- Track cross-tenant access attempts
- Alert on suspicious activity

## Troubleshooting

### Issue: Tenant Context Not Set

**Symptoms**: Requests fail with "Tenant context is required"

**Solution**: 
- Ensure `X-Tenant-Id` or `X-Tenant-Code` header is sent
- Check TenantResolver is registered
- Verify interceptor is not excluded

### Issue: Cross-Tenant Data Access

**Symptoms**: User can see data from other tenants

**Solution**:
- Verify Hibernate filter is enabled
- Check service-level validation
- Review tenant context propagation

### Issue: Performance Issues

**Symptoms**: Slow queries with tenant filtering

**Solution**:
- Ensure indexes on `tenant_id`
- Review query plans
- Consider tenant-specific caching

## Future Enhancements

1. **JWT Integration**: Extract tenant from JWT claims
2. **Tenant-Specific Databases**: Support separate databases per tenant
3. **Tenant Analytics**: Per-tenant usage analytics
4. **Tenant Billing**: Track usage per tenant
5. **Tenant Onboarding**: Automated tenant provisioning

## Files Created

### Core Components
- `Tenant.java` - Tenant entity
- `TenantRepository.java` - Tenant repository
- `TenantContext.java` - Thread-local tenant context
- `TenantResolver.java` - HTTP interceptor
- `TenantAware.java` - Marker interface
- `TenantService.java` - Tenant business logic
- `TenantController.java` - Tenant REST API
- `TenantConfig.java` - Configuration
- `TenantFilter.java` - Hibernate filter

### Database Migrations
- `V7__Create_tenants_table.sql` - Create tenants table
- `V8__Add_tenant_id_to_bookings.sql` - Add tenant_id columns

### Documentation
- `MULTI_TENANCY_IMPLEMENTATION.md` - This file

## Conclusion

Multi-tenancy support is now implemented in the booking service. The same pattern can be applied to other services (train, user, payment, notification) by:

1. Adding tenant-aware interfaces to entities
2. Adding `tenant_id` columns
3. Updating services to use tenant context
4. Adding tenant validation

This provides a solid foundation for SaaS capabilities with proper tenant isolation and security.

