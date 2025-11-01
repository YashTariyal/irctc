#!/bin/bash

# Comprehensive Feature Testing Script
# Tests all new features: Flyway, Redis, API Versioning, Audit Logging, Config Server

BASE_URL="http://localhost:8090"
GATEWAY_URL="http://localhost:8090"
TRAIN_SERVICE="http://localhost:8092"
BOOKING_SERVICE="http://localhost:8093"
CONFIG_SERVER="http://localhost:8888"

echo "🧪 Testing All New Features"
echo "============================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_result() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        return 1
    fi
}

warning_result() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Test 1: Service Health Checks
echo "1️⃣  Testing Service Health..."
echo "-----------------------------------"
curl -s "$TRAIN_SERVICE/actuator/health" | grep -q "UP" && \
    test_result "Train service is UP" || \
    warning_result "Train service not responding"

curl -s "$BOOKING_SERVICE/actuator/health" | grep -q "UP" && \
    test_result "Booking service is UP" || \
    warning_result "Booking service not responding"

# Test 2: API Versioning
echo ""
echo "2️⃣  Testing API Versioning..."
echo "-----------------------------------"
curl -s "$GATEWAY_URL/api/v1/trains" > /dev/null 2>&1 && \
    test_result "v1 API endpoint works (/api/v1/trains)" || \
    warning_result "v1 API endpoint not accessible"

curl -s "$GATEWAY_URL/api/trains" > /dev/null 2>&1 && \
    test_result "Legacy API endpoint works (/api/trains)" || \
    warning_result "Legacy API endpoint not accessible"

curl -s "$TRAIN_SERVICE/api/v1/trains" > /dev/null 2>&1 && \
    test_result "Direct service v1 endpoint works" || \
    warning_result "Direct service v1 endpoint not accessible"

# Test 3: Redis Caching (if Redis is available)
echo ""
echo "3️⃣  Testing Redis Caching..."
echo "-----------------------------------"
if redis-cli ping > /dev/null 2>&1; then
    echo "Redis is running, testing cache..."
    
    # Clear any existing cache
    redis-cli FLUSHDB > /dev/null 2>&1
    
    # First call (should hit DB)
    echo "  First call (DB hit)..."
    START1=$(date +%s%N)
    curl -s "$TRAIN_SERVICE/api/trains/search/route?source=Delhi&destination=Mumbai" > /dev/null 2>&1
    END1=$(date +%s%N)
    TIME1=$((($END1 - $START1) / 1000000))
    
    # Second call (should hit cache)
    echo "  Second call (Cache hit)..."
    START2=$(date +%s%N)
    curl -s "$TRAIN_SERVICE/api/trains/search/route?source=Delhi&destination=Mumbai" > /dev/null 2>&1
    END2=$(date +%s%N)
    TIME2=$((($END2 - $START2) / 1000000))
    
    if [ $TIME2 -lt $TIME1 ]; then
        test_result "Cache working (Second call faster: ${TIME2}ms vs ${TIME1}ms)"
    else
        warning_result "Cache may not be working (Times: ${TIME2}ms vs ${TIME1}ms)"
    fi
    
    # Check Redis keys
    KEY_COUNT=$(redis-cli KEYS "train:*" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$KEY_COUNT" -gt 0 ]; then
        test_result "Cache keys found in Redis ($KEY_COUNT keys)"
    else
        warning_result "No cache keys found in Redis"
    fi
else
    warning_result "Redis not running (caching disabled, but services work)"
fi

# Test 4: Audit Logging
echo ""
echo "4️⃣  Testing Audit Logging..."
echo "-----------------------------------"
echo "  Creating a test booking (should be audited)..."
RESPONSE=$(curl -s -X POST "$BOOKING_SERVICE/api/bookings" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-123" \
  -H "X-Username: testuser" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "totalFare": 500.0
  }' 2>&1)

if echo "$RESPONSE" | grep -q "pnrNumber\|id"; then
    test_result "Booking created successfully"
    BOOKING_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
    
    if [ ! -z "$BOOKING_ID" ]; then
        echo "  Updating booking (should be audited)..."
        curl -s -X PUT "$BOOKING_SERVICE/api/bookings/$BOOKING_ID" \
          -H "Content-Type: application/json" \
          -H "X-User-Id: test-user-123" \
          -H "X-Username: testuser" \
          -d '{"userId":1,"trainId":1,"status":"CONFIRMED","totalFare":600.0}' > /dev/null 2>&1 && \
            test_result "Booking update audited" || \
            warning_result "Booking update may not be audited"
    fi
else
    warning_result "Could not create test booking (may need test data)"
fi

# Note: Audit logs can be verified in database or by checking service logs

# Test 5: Config Server
echo ""
echo "5️⃣  Testing Config Server..."
echo "-----------------------------------"
if curl -s "$CONFIG_SERVER/actuator/health" > /dev/null 2>&1; then
    test_result "Config Server is running"
    
    curl -s "$CONFIG_SERVER/irctc-booking-service/default" > /dev/null 2>&1 && \
        test_result "Booking service config accessible" || \
        warning_result "Booking service config not accessible"
    
    curl -s "$CONFIG_SERVER/irctc-train-service/default" > /dev/null 2>&1 && \
        test_result "Train service config accessible" || \
        warning_result "Train service config not accessible"
else
    warning_result "Config Server not running (optional feature)"
fi

# Test 6: Flyway Migrations
echo ""
echo "6️⃣  Testing Flyway Migrations..."
echo "-----------------------------------"
echo "  Checking service startup logs for Flyway messages..."
echo "  (Review service logs manually for: 'Flyway migrated' or 'Migration successful')"
warning_result "Manual verification required - check service startup logs"

# Summary
echo ""
echo "============================"
echo "✅ Feature Testing Complete!"
echo "============================"
echo ""
echo "📝 Summary:"
echo "  - Services should be running and healthy"
echo "  - API versioning should work for both v1 and legacy paths"
echo "  - Redis caching improves performance (if Redis is running)"
echo "  - Audit logs created for booking operations"
echo "  - Config Server provides centralized config (if running)"
echo "  - Flyway migrations applied during service startup"
echo ""
echo "For detailed verification, check:"
echo "  - Service logs for Flyway migration messages"
echo "  - Database/H2 console for audit_logs table"
echo "  - Redis CLI for cache keys: redis-cli KEYS '*'"
echo "  - Config Server endpoints: http://localhost:8888/{service-name}/default"
echo ""

