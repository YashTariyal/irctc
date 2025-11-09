# Request/Response Logging Middleware Implementation Guide

## Overview

Comprehensive request/response logging middleware for all microservices with sensitive data masking, performance metrics, correlation ID support, and configurable logging levels.

## Features

### 1. **Complete Request/Response Logging**
- ✅ Request method, URI, query parameters
- ✅ Request headers (with sensitive data masking)
- ✅ Request body (configurable)
- ✅ Response status code
- ✅ Response body (configurable)
- ✅ Client IP address
- ✅ User-Agent information

### 2. **Sensitive Data Masking**
- ✅ Passwords, tokens, secrets
- ✅ API keys and authorization headers
- ✅ Credit card numbers, CVV, PIN
- ✅ SSN and account numbers
- ✅ PII (email, phone) - partial masking
- ✅ JSON and non-JSON data support

### 3. **Performance Metrics**
- ✅ Request duration tracking
- ✅ Slow request detection
- ✅ Prometheus metrics integration
- ✅ Request count by method/path/status

### 4. **Correlation ID Support**
- ✅ Automatic correlation ID propagation
- ✅ MDC (Mapped Diagnostic Context) integration
- ✅ Request tracing across services

### 5. **Configurable Logging**
- ✅ Enable/disable logging
- ✅ Request body logging toggle
- ✅ Response body logging toggle
- ✅ Maximum body size limits
- ✅ Slow request threshold
- ✅ PII masking toggle

## Configuration

### Application Properties

```yaml
logging:
  request-response:
    enabled: true                    # Enable/disable request/response logging
    log-request-body: true          # Log request body
    log-response-body: true         # Log response body
    max-body-size: 10000           # Maximum body size to log (characters)
    slow-request-threshold: 2000    # Log warning if request takes longer (ms)
    mask-pii: true                 # Mask personally identifiable information
```

## Sensitive Data Masking

### Masked Fields

The following fields are automatically masked:

**Authentication & Security:**
- `password`, `pwd`, `passwd`, `pass`
- `token`, `accessToken`, `refreshToken`, `authToken`, `bearer`
- `secret`, `apiKey`, `apikey`, `api_key`
- `authorization`, `x-api-key`, `x-auth-token`

**Payment Information:**
- `cardNumber`, `card_number`, `creditCard`, `credit_card`
- `cvv`, `cvc`, `cvv2`
- `pin`, `accountNumber`, `routingNumber`

**Personal Information (PII):**
- `ssn`, `socialSecurityNumber`
- `email`, `phone`, `phoneNumber` (partial masking)

### Masking Examples

**Before:**
```json
{
  "username": "john.doe",
  "password": "mySecretPassword123",
  "email": "john.doe@example.com",
  "cardNumber": "1234567890123456"
}
```

**After:**
```json
{
  "username": "john.doe",
  "password": "***",
  "email": "john***@example.com",
  "cardNumber": "1234***3456"
}
```

## Log Format

### Request Log

```
📥 REQUEST [correlation-id] POST /api/bookings | IP: 192.168.1.1 | User-Agent: Mozilla/5.0 | Headers: {...} | Body: {...}
```

### Response Log

```
✅ RESPONSE [correlation-id] POST /api/bookings | Status: 201 | Duration: 45ms | Body: {...}
```

### Status Emojis

- ✅ `200-299` - Success
- ↪️ `300-399` - Redirect
- ⚠️ `400-499` - Client Error
- ❌ `500+` - Server Error

## Excluded Paths

The following paths are excluded from logging:

- `/actuator/health`
- `/actuator/prometheus`
- `/actuator/info`
- `/actuator/metrics`
- `/swagger-ui`
- `/api-docs`

## Performance Metrics

### Prometheus Metrics

The filter automatically records the following metrics:

**Request Duration:**
```
http_request_duration_seconds{method="POST",path="/api/bookings",status="201"}
```

**Request Count:**
```
http_request_total{method="POST",path="/api/bookings",status="201"}
```

### Slow Request Detection

Requests taking longer than the configured threshold are logged as warnings:

```
⚠️  SLOW REQUEST [correlation-id] POST /api/bookings took 2500ms (threshold: 2000ms)
```

## Correlation ID

### Automatic Generation

If no correlation ID is present in MDC, one is automatically generated:

```java
String correlationId = MDC.get("correlationId");
if (correlationId == null) {
    correlationId = UUID.randomUUID().toString();
    MDC.put("correlationId", correlationId);
}
```

### Propagation

Correlation IDs are automatically propagated through:
- MDC (Mapped Diagnostic Context)
- Log messages
- Request tracing

## Client IP Detection

The filter detects client IP from multiple sources:

1. `X-Forwarded-For` header (for load balancers/proxies)
2. `X-Real-IP` header
3. `request.getRemoteAddr()` (fallback)

## Body Size Limits

Large request/response bodies are truncated:

```
Body: {...truncated content...}... (truncated)
```

Default limit: 10,000 characters (configurable)

## Log Levels

### Automatic Log Level Selection

- **ERROR**: Status code >= 500
- **WARN**: Status code >= 400
- **INFO**: Status code < 400

### Example

```java
if (status >= 500) {
    logger.error(message);
} else if (status >= 400) {
    logger.warn(message);
} else {
    logger.info(message);
}
```

## JSON Masking

The filter intelligently masks JSON data:

```json
{
  "user": {
    "username": "john",
    "password": "***",  // Masked
    "email": "john***@example.com"  // Partially masked
  },
  "payment": {
    "cardNumber": "1234***3456",  // Masked
    "amount": 100.00  // Not masked
  }
}
```

## Path Sanitization

For better metric aggregation, path parameters are sanitized:

- `/api/bookings/123` → `/api/bookings/{id}`
- `/api/users/a1b2c3d4-e5f6-7890-abcd-ef1234567890` → `/api/users/{uuid}`
- `/api/trains/EXPRESS123` → `/api/trains/{param}`

## Usage Examples

### Basic Request/Response

**Request:**
```http
POST /api/bookings
Content-Type: application/json

{
  "userId": 1,
  "trainId": 2,
  "passengers": [...]
}
```

**Log Output:**
```
📥 REQUEST [abc-123] POST /api/bookings | IP: 192.168.1.1 | User-Agent: PostmanRuntime/7.29.0 | Headers: {"Content-Type":"application/json"} | Body: {"userId":1,"trainId":2,"passengers":[...]}
✅ RESPONSE [abc-123] POST /api/bookings | Status: 201 | Duration: 45ms | Body: {"id":10,"pnrNumber":"ABC123","status":"CONFIRMED"}
```

### Request with Sensitive Data

**Request:**
```http
POST /api/users/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "secret123"
}
```

**Log Output:**
```
📥 REQUEST [abc-123] POST /api/users/login | IP: 192.168.1.1 | Headers: {...} | Body: {"username":"john.doe","password":"***"}
✅ RESPONSE [abc-123] POST /api/users/login | Status: 200 | Duration: 12ms | Body: {"token":"***","user":{...}}
```

### Slow Request

**Request taking 2500ms:**
```
📥 REQUEST [abc-123] GET /api/bookings/1
⚠️  SLOW REQUEST [abc-123] GET /api/bookings/1 took 2500ms (threshold: 2000ms)
✅ RESPONSE [abc-123] GET /api/bookings/1 | Status: 200 | Duration: 2500ms
```

## Best Practices

### 1. **Enable Selectively**
- Enable in development/staging
- Consider disabling in production for high-traffic endpoints
- Use configuration to toggle per environment

### 2. **Body Size Limits**
- Set appropriate `max-body-size` to prevent log bloat
- Large file uploads should be excluded

### 3. **Sensitive Data**
- Always enable `mask-pii: true` in production
- Review and add custom sensitive fields as needed
- Test masking with real data patterns

### 4. **Performance**
- Monitor slow request warnings
- Adjust `slow-request-threshold` based on SLA
- Use metrics for performance analysis

### 5. **Correlation IDs**
- Ensure correlation IDs are propagated across services
- Use correlation IDs for request tracing
- Include correlation IDs in error reports

## Troubleshooting

### Logs Not Appearing

1. Check `logging.request-response.enabled: true`
2. Verify path is not in excluded paths
3. Check log level configuration
4. Review filter order in filter chain

### Sensitive Data Not Masked

1. Verify field name matches sensitive fields list
2. Check JSON structure (masking works on JSON objects)
3. Review regex patterns for non-JSON data
4. Add custom fields to `SENSITIVE_FIELDS` set

### Performance Impact

1. Disable body logging for high-traffic endpoints
2. Reduce `max-body-size` limit
3. Exclude health check endpoints
4. Monitor metrics for filter overhead

## Files Created

### Enhanced Filters
- `EnhancedRequestResponseLoggingFilter.java` (all 5 services)

### Configuration Updates
- `application.yml` (all services) - Added logging configuration

## Migration from Old Filter

The old `RequestResponseLoggingFilter` can coexist with the enhanced version. To migrate:

1. Keep both filters temporarily
2. Test enhanced filter in staging
3. Remove old filter once verified
4. Update configuration as needed

## Benefits

1. **Security**
   - Automatic sensitive data masking
   - PII protection
   - Compliance support

2. **Observability**
   - Complete request/response visibility
   - Correlation ID tracking
   - Performance metrics

3. **Debugging**
   - Full request/response context
   - Error tracking
   - Slow request detection

4. **Compliance**
   - Audit trail
   - Data protection
   - Security logging

## Conclusion

Enhanced Request/Response Logging Middleware provides comprehensive logging with security, performance, and observability features. The implementation ensures sensitive data protection while maintaining full request/response visibility for debugging and monitoring.

