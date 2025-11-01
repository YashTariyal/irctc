# 🧪 Complete Feature Testing Guide

This guide provides step-by-step instructions for testing all newly implemented features in a running environment.

## 📋 Prerequisites

1. **Infrastructure Services:**
   ```bash
   # Start Docker containers (if using Docker)
   docker-compose up -d postgres redis kafka zookeeper
   
   # OR use local services
   # Redis should be running on localhost:6379
   # Kafka should be running on localhost:9092
   ```

2. **Eureka Server:**
   ```bash
   cd irctc-eureka-server
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Wait for it to start on http://localhost:8761
   ```

## 🚀 Starting Services

### Option 1: Start All Services (Recommended)

Use the existing startup script:
```bash
./start-microservices.sh
```

### Option 2: Start Services Individually

1. **Config Server (Optional but Recommended):**
   ```bash
   cd irctc-config-server
   ./mvnw spring-boot:run
   # Starts on http://localhost:8888
   ```

2. **API Gateway:**
   ```bash
   cd irctc-api-gateway
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Starts on http://localhost:8090
   ```

3. **User Service:**
   ```bash
   cd irctc-user-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Starts on http://localhost:8091
   ```

4. **Train Service:**
   ```bash
   cd irctc-train-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Starts on http://localhost:8092
   ```

5. **Booking Service:**
   ```bash
   cd irctc-booking-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Starts on http://localhost:8093
   ```

6. **Payment Service:**
   ```bash
   cd irctc-payment-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Starts on http://localhost:8094
   ```

7. **Notification Service:**
   ```bash
   cd irctc-notification-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   # Starts on http://localhost:8095
   ```

---

## ✅ Feature Testing

### 1. Test Flyway Database Migrations

**Objective:** Verify that Flyway creates all database tables correctly.

**Steps:**
1. Check service logs during startup for Flyway messages:
   ```bash
   # Look for messages like:
   # "Flyway migrated schema to version 1"
   # "Successfully applied migration V1__Initial_booking_schema"
   ```

2. Verify tables were created (using H2 Console or logs):
   ```bash
   # For booking service, check for:
   # - bookings
   # - passengers
   # - outbox_events
   # - idempotency_keys
   # - shedlock
   # - audit_logs (NEW)
   ```

3. Test by checking health endpoint:
   ```bash
   curl http://localhost:8093/actuator/health
   # Should return {"status":"UP"}
   ```

**Expected Result:**
- Services start without database errors
- All tables created successfully
- Flyway migration logs visible in console

---

### 2. Test Redis Caching

**Objective:** Verify that Redis caching improves performance and reduces database load.

**Prerequisites:**
```bash
# Start Redis (if not using Docker)
redis-server

# OR using Docker
docker run -d -p 6379:6379 redis:7-alpine
```

**Test Train Service Caching:**
```bash
# First call - should hit database
echo "=== First call (DB hit) ==="
time curl -s http://localhost:8092/api/trains/search/route?source=Delhi\&destination=Mumbai | jq '.[0]'

# Second call - should hit cache (much faster)
echo "=== Second call (Cache hit) ==="
time curl -s http://localhost:8092/api/trains/search/route?source=Delhi\&destination=Mumbai | jq '.[0]'

# Verify cache by checking Redis
redis-cli KEYS "train:*"
```

**Test Booking Service Caching:**
```bash
# Create a booking first (if you have test data)
BOOKING_ID=1
PNR="TEST123"

# First call - should hit database
echo "=== First call (DB hit) ==="
time curl -s http://localhost:8093/api/bookings/$BOOKING_ID | jq

# Second call - should hit cache
echo "=== Second call (Cache hit) ==="
time curl -s http://localhost:8093/api/bookings/$BOOKING_ID | jq

# Verify cache
redis-cli KEYS "booking:*"
```

**Expected Result:**
- First call takes longer (hits database)
- Second call is faster (uses cache)
- Redis keys visible for cached data

**Note:** If Redis is not running, services will work without caching (graceful degradation).

---

### 3. Test API Versioning

**Objective:** Verify that both versioned and legacy API endpoints work.

**Test Versioned Endpoints:**
```bash
# Version 1 API (NEW)
echo "=== Testing v1 API ==="
curl -s http://localhost:8090/api/v1/trains | jq '.[0] | {trainNumber, trainName}'
curl -s http://localhost:8090/api/v1/bookings | jq 'length'
```

**Test Legacy Endpoints (Backward Compatibility):**
```bash
# Legacy API (still works)
echo "=== Testing legacy API ==="
curl -s http://localhost:8090/api/trains | jq '.[0] | {trainNumber, trainName}'
curl -s http://localhost:8090/api/bookings | jq 'length'
```

**Test Direct Service Endpoints:**
```bash
# Direct service access (bypassing gateway)
curl -s http://localhost:8092/api/v1/trains
curl -s http://localhost:8093/api/v1/bookings
```

**Expected Result:**
- Both `/api/v1/...` and `/api/...` endpoints return the same data
- API Gateway routes work correctly
- Services respond to both versioned and non-versioned paths

---

### 4. Test Audit Logging

**Objective:** Verify that audit logs are created for audited operations.

**Create a Booking (Audited Operation):**
```bash
# Create a booking - this should be audited
curl -X POST http://localhost:8093/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-123" \
  -H "X-Username: testuser" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "totalFare": 500.0,
    "passengers": []
  }' | jq
```

**Update a Booking (Audited Operation):**
```bash
# Get booking ID from previous response
BOOKING_ID=1

curl -X PUT http://localhost:8093/api/bookings/$BOOKING_ID \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-123" \
  -H "X-Username: testuser" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "status": "CONFIRMED",
    "totalFare": 600.0
  }' | jq
```

**Cancel a Booking (Audited Operation):**
```bash
curl -X DELETE http://localhost:8093/api/bookings/$BOOKING_ID \
  -H "X-User-Id: test-user-123" \
  -H "X-Username: testuser"
```

**Query Audit Logs:**

Since we don't have an audit log endpoint yet, check the database directly or add a query endpoint:

**Option 1: Add temporary endpoint for testing:**
```bash
# Query via service logs or database
# Look in booking service logs for: "Saved audit log"
```

**Option 2: Query database directly (H2 Console):**
```
http://localhost:8093/h2-console
JDBC URL: jdbc:h2:mem:testdb
User: sa
Password: (empty)

Run: SELECT * FROM audit_logs ORDER BY timestamp DESC;
```

**Expected Audit Log Fields:**
- `entity_type`: "Booking"
- `entity_id`: Booking ID
- `action`: "CREATE", "UPDATE", or "DELETE"
- `user_id`: "test-user-123"
- `username`: "testuser"
- `ip_address`: Client IP
- `http_method`: "POST", "PUT", "DELETE"
- `request_path`: "/api/bookings"
- `request_body`: JSON payload (for CREATE/UPDATE)
- `timestamp`: When action occurred

**Expected Result:**
- Audit logs created for all audited operations
- All relevant fields populated
- Can query audit history by entity

---

### 5. Test Config Server

**Objective:** Verify that Config Server provides centralized configuration.

**Start Config Server:**
```bash
cd irctc-config-server
./mvnw spring-boot:run
# Starts on http://localhost:8888
```

**Test Config Server Endpoints:**
```bash
# Get default configuration
curl http://localhost:8888/application/default

# Get booking service configuration
curl http://localhost:8888/irctc-booking-service/default

# Get train service configuration
curl http://localhost:8888/irctc-train-service/default

# Get user service configuration
curl http://localhost:8888/irctc-user-service/default
```

**Enable Config Client in Services:**

To test Config Server integration, enable it:
```bash
# Set environment variable
export CONFIG_SERVER_ENABLED=true

# Restart a service (e.g., booking service)
cd irctc-booking-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Verify Config Server Connection:**
```bash
# Check service logs for:
# "Located property source: [ConfigServerPropertySource"
# "Fetching config from server at: http://localhost:8888"
```

**Test Runtime Configuration Refresh:**
```bash
# Update a config file
echo "test: updated-value" >> config-repo/irctc-booking-service.yml

# Trigger refresh (if actuator refresh endpoint enabled)
curl -X POST http://localhost:8093/actuator/refresh \
  -H "Content-Type: application/json"

# Verify new config value
curl http://localhost:8888/irctc-booking-service/default
```

**Expected Result:**
- Config Server serves configuration files
- Services can connect to Config Server (optional)
- Configuration can be refreshed at runtime

---

## 🔍 Integration Testing Script

Create a comprehensive test script:

```bash
#!/bin/bash
# save as test-all-features.sh

BASE_URL="http://localhost:8090"
echo "🧪 Testing All Features"
echo "========================"

# Test 1: API Versioning
echo ""
echo "1️⃣  Testing API Versioning..."
curl -s "$BASE_URL/api/v1/trains" > /dev/null && echo "✅ v1 API works" || echo "❌ v1 API failed"
curl -s "$BASE_URL/api/trains" > /dev/null && echo "✅ Legacy API works" || echo "❌ Legacy API failed"

# Test 2: Create Booking (Audit Test)
echo ""
echo "2️⃣  Testing Audit Logging..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/bookings" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-123" \
  -d '{"userId":1,"trainId":1,"totalFare":500.0}')
echo "$RESPONSE" | jq -r '.pnrNumber // "Failed"' | xargs -I {} echo "✅ Booking created: {}"

# Test 3: Cache Test (requires Redis)
echo ""
echo "3️⃣  Testing Redis Caching..."
FIRST_TIME=$(time curl -s "$BASE_URL/api/trains/search/route?source=Delhi&destination=Mumbai" > /dev/null 2>&1)
SECOND_TIME=$(time curl -s "$BASE_URL/api/trains/search/route?source=Delhi&destination=Mumbai" > /dev/null 2>&1)
echo "✅ Cache test completed (check Redis for keys)"

# Test 4: Config Server
echo ""
echo "4️⃣  Testing Config Server..."
curl -s http://localhost:8888/irctc-booking-service/default > /dev/null && \
  echo "✅ Config Server responding" || echo "⚠️  Config Server not running (optional)"

echo ""
echo "✅ All tests completed!"
```

---

## 📊 Verification Checklist

After running all tests, verify:

- [ ] **Flyway Migrations:**
  - [ ] Services start without database errors
  - [ ] All tables created (check logs)
  - [ ] Migration logs visible

- [ ] **Redis Caching:**
  - [ ] First call slower (hits DB)
  - [ ] Second call faster (uses cache)
  - [ ] Redis keys visible: `redis-cli KEYS "*"`

- [ ] **API Versioning:**
  - [ ] `/api/v1/trains` works
  - [ ] `/api/trains` works (backward compatible)
  - [ ] API Gateway routes correctly

- [ ] **Audit Logging:**
  - [ ] Audit logs created for CREATE
  - [ ] Audit logs created for UPDATE
  - [ ] Audit logs created for DELETE
  - [ ] All audit fields populated

- [ ] **Config Server:**
  - [ ] Config Server responds on port 8888
  - [ ] Service configs accessible
  - [ ] Services can connect (optional)

---

## 🐛 Troubleshooting

### Issue: Services fail to start
**Solution:** Check prerequisites (Eureka, Redis, Kafka)

### Issue: Redis caching not working
**Solution:** 
- Verify Redis is running: `redis-cli ping`
- Check Redis connection in service logs
- Services work without Redis (graceful degradation)

### Issue: Audit logs not created
**Solution:**
- Check AOP is enabled (spring-boot-starter-aop in pom.xml)
- Verify @Auditable annotations are present
- Check service logs for errors

### Issue: Config Server not accessible
**Solution:**
- Config Server is optional
- Services work without it
- Check port 8888 is not in use

### Issue: Flyway migrations fail
**Solution:**
- Check migration files in `db/migration/`
- Verify SQL syntax
- Check database connection

---

## 📝 Next Steps

1. **Extend Audit Logging:**
   - Add audit endpoints for querying logs
   - Apply @Auditable to other services
   - Add audit log cleanup job

2. **Enhance Caching:**
   - Add cache metrics
   - Implement cache warming
   - Add cache eviction policies

3. **Config Server:**
   - Switch to Git backend
   - Add encryption for sensitive configs
   - Set up Config Server HA

---

**Happy Testing! 🚀**

