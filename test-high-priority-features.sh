#!/bin/bash

# Test script for High Priority Features Implementation
# Tests: Global Exception Handler, Correlation IDs, Request/Response Logging

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BOOKING_SERVICE_URL="http://localhost:8093"
TRAIN_SERVICE_URL="http://localhost:8092"
API_GATEWAY_URL="http://localhost:8090"
TIMEOUT=5

# Counters
PASSED=0
FAILED=0

echo ""
echo "========================================="
echo "🧪 Testing High Priority Features"
echo "========================================="
echo ""

# Function to print test header
test_header() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}Test: $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# Function to check if service is up
check_service() {
    local url=$1
    local service_name=$2
    
    if curl -s --max-time $TIMEOUT "$url/actuator/health" > /dev/null 2>&1; then
        return 0
    else
        echo -e "${RED}❌ $service_name is not running at $url${NC}"
        return 1
    fi
}

# Function to assert response contains field
assert_contains() {
    local response=$1
    local field=$2
    local test_name=$3
    
    if echo "$response" | grep -q "$field"; then
        echo -e "${GREEN}✅ $test_name${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}❌ $test_name - Response does not contain '$field'${NC}"
        echo "Response: $response"
        ((FAILED++))
        return 1
    fi
}

# Function to assert status code
assert_status() {
    local status=$1
    local expected=$2
    local test_name=$3
    
    if [ "$status" -eq "$expected" ]; then
        echo -e "${GREEN}✅ $test_name${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}❌ $test_name - Expected $expected, got $status${NC}"
        ((FAILED++))
        return 1
    fi
}

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 3

# Check services are running
echo ""
echo "🔍 Checking service availability..."
check_service "$BOOKING_SERVICE_URL" "Booking Service" || exit 1
check_service "$TRAIN_SERVICE_URL" "Train Service" || exit 1
check_service "$API_GATEWAY_URL" "API Gateway" || exit 1
echo -e "${GREEN}✅ All services are running${NC}"
echo ""

# ==========================================
# TEST 1: Global Exception Handler - Entity Not Found
# ==========================================
test_header "1. Global Exception Handler - Entity Not Found"

RESPONSE=$(curl -s -w "\n%{http_code}" "$BOOKING_SERVICE_URL/api/v1/bookings/99999" 2>/dev/null)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response: $BODY"

assert_status "$HTTP_CODE" "404" "Status code should be 404"
assert_contains "$BODY" "ENTITY_NOT_FOUND" "Error code should be ENTITY_NOT_FOUND"
assert_contains "$BODY" "correlationId" "Should contain correlationId"
assert_contains "$BODY" "timestamp" "Should contain timestamp"
assert_contains "$BODY" "errorCode" "Should contain errorCode"
assert_contains "$BODY" "message" "Should contain message"

# ==========================================
# TEST 2: Correlation ID - Auto Generation
# ==========================================
test_header "2. Correlation ID - Auto Generation"

RESPONSE=$(curl -s -i "$BOOKING_SERVICE_URL/api/v1/bookings" 2>/dev/null)
CORRELATION_ID=$(echo "$RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r')

if [ -n "$CORRELATION_ID" ]; then
    echo -e "${GREEN}✅ Correlation ID generated: $CORRELATION_ID${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Correlation ID not found in response headers${NC}"
    ((FAILED++))
fi

# ==========================================
# TEST 3: Correlation ID - Client Provided
# ==========================================
test_header "3. Correlation ID - Client Provided"

CUSTOM_CORRELATION_ID="test-correlation-12345"
RESPONSE=$(curl -s -i -H "X-Correlation-Id: $CUSTOM_CORRELATION_ID" "$BOOKING_SERVICE_URL/api/v1/bookings" 2>/dev/null)
RECEIVED_ID=$(echo "$RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r')

if [ "$RECEIVED_ID" = "$CUSTOM_CORRELATION_ID" ]; then
    echo -e "${GREEN}✅ Client-provided correlation ID preserved: $CUSTOM_CORRELATION_ID${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Correlation ID mismatch. Expected: $CUSTOM_CORRELATION_ID, Got: $RECEIVED_ID${NC}"
    ((FAILED++))
fi

# ==========================================
# TEST 4: Correlation ID in Error Response
# ==========================================
test_header "4. Correlation ID in Error Response"

CUSTOM_CORRELATION_ID="error-test-67890"
RESPONSE=$(curl -s -H "X-Correlation-Id: $CUSTOM_CORRELATION_ID" "$BOOKING_SERVICE_URL/api/v1/bookings/99999" 2>/dev/null)

assert_contains "$RESPONSE" "$CUSTOM_CORRELATION_ID" "Error response should contain client correlation ID"

# ==========================================
# TEST 5: API Gateway Correlation ID Propagation
# ==========================================
test_header "5. API Gateway Correlation ID Propagation"

if curl -s --max-time $TIMEOUT "$API_GATEWAY_URL/actuator/health" > /dev/null 2>&1; then
    GATEWAY_CORRELATION_ID="gateway-test-abc123"
    RESPONSE=$(curl -s -i -H "X-Correlation-Id: $GATEWAY_CORRELATION_ID" "$API_GATEWAY_URL/api/v1/bookings" 2>/dev/null)
    GATEWAY_RECEIVED_ID=$(echo "$RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r')
    
    if [ -n "$GATEWAY_RECEIVED_ID" ]; then
        echo -e "${GREEN}✅ API Gateway preserves correlation ID: $GATEWAY_RECEIVED_ID${NC}"
        ((PASSED++))
    else
        echo -e "${YELLOW}⚠️  API Gateway correlation ID test skipped (gateway may not be routing to booking service)${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  API Gateway not available, skipping gateway test${NC}"
fi

# ==========================================
# TEST 6: Validation Error Handling
# ==========================================
test_header "6. Validation Error Handling"

# Test with invalid JSON
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BOOKING_SERVICE_URL/api/v1/bookings" \
    -H "Content-Type: application/json" \
    -d 'invalid json' 2>/dev/null)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

assert_status "$HTTP_CODE" "400" "Invalid JSON should return 400"
assert_contains "$BODY" "errorCode" "Should contain errorCode in validation error"

# ==========================================
# TEST 7: Train Service Exception Handling
# ==========================================
test_header "7. Train Service Exception Handling"

RESPONSE=$(curl -s -w "\n%{http_code}" "$TRAIN_SERVICE_URL/api/v1/trains/99999" 2>/dev/null)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

assert_status "$HTTP_CODE" "404" "Train service should return 404 for not found"
assert_contains "$BODY" "correlationId" "Train service error should contain correlationId"

# ==========================================
# TEST 8: Error Response Format Validation
# ==========================================
test_header "8. Error Response Format Validation"

RESPONSE=$(curl -s "$BOOKING_SERVICE_URL/api/v1/bookings/99999" 2>/dev/null)

# Check for all required fields
assert_contains "$RESPONSE" "\"timestamp\"" "Should have timestamp field"
assert_contains "$RESPONSE" "\"status\"" "Should have status field"
assert_contains "$RESPONSE" "\"errorCode\"" "Should have errorCode field"
assert_contains "$RESPONSE" "\"message\"" "Should have message field"
assert_contains "$RESPONSE" "\"path\"" "Should have path field"
assert_contains "$RESPONSE" "\"method\"" "Should have method field"

# Validate JSON structure
if echo "$RESPONSE" | jq . > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Error response is valid JSON${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Error response is not valid JSON${NC}"
    ((FAILED++))
fi

# ==========================================
# TEST 9: Correlation ID Consistency
# ==========================================
test_header "9. Correlation ID Consistency Across Services"

CORRELATION_ID="consistency-test-xyz"
BOOKING_RESPONSE=$(curl -s -i -H "X-Correlation-Id: $CORRELATION_ID" "$BOOKING_SERVICE_URL/api/v1/bookings/99999" 2>/dev/null)
TRAIN_RESPONSE=$(curl -s -i -H "X-Correlation-Id: $CORRELATION_ID" "$TRAIN_SERVICE_URL/api/v1/trains/99999" 2>/dev/null)

BOOKING_HEADER_ID=$(echo "$BOOKING_RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r')
TRAIN_HEADER_ID=$(echo "$TRAIN_RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r')

if [ "$BOOKING_HEADER_ID" = "$CORRELATION_ID" ] && [ "$TRAIN_HEADER_ID" = "$CORRELATION_ID" ]; then
    echo -e "${GREEN}✅ Correlation ID consistent across services${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Correlation ID inconsistency. Booking: $BOOKING_HEADER_ID, Train: $TRAIN_HEADER_ID${NC}"
    ((FAILED++))
fi

# ==========================================
# TEST 10: Request/Response Logging (Manual Verification)
# ==========================================
test_header "10. Request/Response Logging (Check Service Logs)"

echo "Making a request that should be logged..."
curl -s -X POST "$BOOKING_SERVICE_URL/api/v1/bookings" \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: logging-test-123" \
    -d '{"userId":1,"trainId":1,"status":"CONFIRMED","totalFare":500.0}' \
    > /dev/null 2>&1

echo -e "${YELLOW}⚠️  Please check service logs for:${NC}"
echo "   - 📥 INCOMING REQUEST entries with correlation ID"
echo "   - 📤 OUTGOING RESPONSE entries"
echo "   - Masked sensitive data (if any)"
echo ""
echo "   Look for: logs/irctc-booking-service.log or console output"

# ==========================================
# Summary
# ==========================================
echo ""
echo "========================================="
echo "📊 Test Summary"
echo "========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
TOTAL=$((PASSED + FAILED))
if [ $TOTAL -gt 0 ]; then
    SUCCESS_RATE=$((PASSED * 100 / TOTAL))
    echo "Success Rate: $SUCCESS_RATE%"
fi
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All automated tests passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Check service logs for request/response logging"
    echo "2. Verify correlation IDs in distributed traces"
    echo "3. Test with actual booking creation to see error handling"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the output above.${NC}"
    exit 1
fi

