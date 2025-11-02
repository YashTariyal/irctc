# 🧪 Testing Guide - High Priority Features

## Prerequisites

Before running tests, ensure all required services are running:

1. **Eureka Server** - Port 8761
2. **Booking Service** - Port 8093
3. **Train Service** - Port 8092
4. **API Gateway** - Port 8090 (optional but recommended)

## Starting Services

### Option 1: Start All Services
```bash
# Start all microservices using the existing script
./start-microservices.sh
```

### Option 2: Start Individual Services
```bash
# Terminal 1 - Eureka Server
cd irctc-eureka-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2 - Booking Service
cd irctc-booking-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3 - Train Service
cd irctc-train-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 4 - API Gateway (optional)
cd irctc-api-gateway
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 3: Docker Compose (if available)
```bash
docker-compose up -d
```

## Wait for Services to Start

Give services 30-60 seconds to fully start:
```bash
# Check service health
curl http://localhost:8093/actuator/health  # Booking Service
curl http://localhost:8092/actuator/health  # Train Service
curl http://localhost:8090/actuator/health  # API Gateway
```

## Running Tests

### Automated Test Script
```bash
# Make executable (if not already)
chmod +x test-high-priority-features.sh

# Run tests
./test-high-priority-features.sh
```

### Manual Testing

#### 1. Test Global Exception Handler

```bash
# Test entity not found (should return 404 with structured error)
curl -v http://localhost:8093/api/v1/bookings/99999

# Expected response:
# - Status: 404
# - JSON body with: errorCode, message, correlationId, timestamp
```

#### 2. Test Correlation ID - Auto Generation

```bash
# Request without correlation ID (service should generate one)
curl -i http://localhost:8093/api/v1/bookings

# Check response header for X-Correlation-Id
```

#### 3. Test Correlation ID - Client Provided

```bash
# Request with custom correlation ID
curl -i -H "X-Correlation-Id: my-test-id-123" \
  http://localhost:8093/api/v1/bookings

# Response header should contain: X-Correlation-Id: my-test-id-123
```

#### 4. Test Correlation ID in Error Response

```bash
# Request with correlation ID, trigger error
curl -H "X-Correlation-Id: error-test-456" \
  http://localhost:8093/api/v1/bookings/99999

# Response JSON should contain correlationId field with value: error-test-456
```

#### 5. Test Validation Error Handling

```bash
# Invalid JSON request
curl -X POST http://localhost:8093/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d 'invalid json'

# Expected: 400 status with validation error details

# Missing required fields
curl -X POST http://localhost:8093/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'

# Expected: 400 status with field validation errors
```

#### 6. Test Train Service Exception Handling

```bash
# Non-existent train
curl http://localhost:8092/api/v1/trains/99999

# Expected: 404 with structured error response including correlationId
```

#### 7. Test API Gateway Correlation ID

```bash
# Through gateway (if running)
curl -i -H "X-Correlation-Id: gateway-test" \
  http://localhost:8090/api/v1/bookings

# Response should contain correlation ID in headers
```

#### 8. Test Request/Response Logging

```bash
# Make a request
curl -X POST http://localhost:8093/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: logging-test" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "status": "CONFIRMED",
    "totalFare": 500.0
  }'

# Check booking service logs for:
# - 📥 INCOMING REQUEST entry with correlation ID
# - 📤 OUTGOING RESPONSE entry
# - Sensitive data should be masked if present
```

Check logs:
```bash
# If logs are in files
tail -f irctc-booking-service/logs/*.log

# Or check console output where service is running
```

#### 9. Verify Error Response Format

```bash
# Get error response
RESPONSE=$(curl -s http://localhost:8093/api/v1/bookings/99999)

# Verify JSON structure (if jq is installed)
echo "$RESPONSE" | jq .

# Should contain:
# - timestamp (ISO format)
# - status (number)
# - errorCode (string)
# - message (string)
# - path (string)
# - method (string)
# - correlationId (string)
# - traceId (string, if tracing enabled)
```

## Expected Log Output

When request/response logging is working, you should see in service logs:

```
2024-12-28 10:30:00 - 📥 INCOMING REQUEST [550e8400-e29b-41d4-a716-446655440000] POST /api/v1/bookings - Headers: {...} - Body: {"userId":1,...}
2024-12-28 10:30:01 - 📤 OUTGOING RESPONSE [550e8400-e29b-41d4-a716-446655440000] POST /api/v1/bookings - Status: 200 - Body: {"id":1,...}
```

## Troubleshooting

### Services Not Starting
- Check if ports are already in use: `lsof -i :8093`
- Check Java version: `java -version` (should be 17+)
- Check Maven: `./mvnw --version`

### Tests Failing
- Ensure services are fully started (wait 60 seconds)
- Check service health endpoints
- Review service logs for errors
- Verify database is accessible (H2 should start automatically)

### Correlation ID Not Appearing
- Check if filters are properly registered
- Verify filter order (@Order annotation)
- Check service logs for filter execution

### Error Responses Not Structured
- Verify GlobalExceptionHandler is annotated with @ControllerAdvice
- Check if exception classes extend CustomException
- Verify Jackson is in classpath for JSON serialization

## Test Results Interpretation

### ✅ Success Indicators
- HTTP status codes match expected values
- Error responses have consistent JSON structure
- Correlation IDs are present in headers and error bodies
- Logs show request/response entries with correlation IDs

### ❌ Failure Indicators
- Services return 500 instead of proper error codes
- Error responses are plain text instead of JSON
- Correlation IDs missing from headers or responses
- No request/response logging visible

## Next Steps After Testing

Once tests pass:
1. ✅ Replicate features to remaining services (User, Notification, Payment)
2. ✅ Add correlation ID to Kafka message headers
3. ✅ Integrate correlation ID with OpenTelemetry traces
4. ✅ Add request/response logging to all services
5. ✅ Create Grafana dashboard for correlation ID tracking

---

**Last Updated**: 2024-12-28

