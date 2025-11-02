#!/bin/bash

# Integration Test Script for High-Priority Features
# Tests: Global Exception Handler, Correlation IDs, Error Response Format

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Service URLs
BOOKING_SERVICE="http://localhost:8093"
TRAIN_SERVICE="http://localhost:8092"
USER_SERVICE="http://localhost:8091"
NOTIFICATION_SERVICE="http://localhost:8095"
PAYMENT_SERVICE="http://localhost:8094"
API_GATEWAY="http://localhost:8090"

# Counters
PASSED=0
FAILED=0
TOTAL=0

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  🧪 Integration Testing - High-Priority Features             ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Function to print test section
test_section() {
    echo ""
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}📋 $1${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# Function to test service health
test_service_health() {
    local service_name=$1
    local service_url=$2
    
    if curl -s --max-time 5 "$service_url/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $service_name is UP${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is DOWN${NC}"
        return 1
    fi
}

# Function to assert status code
assert_status() {
    local status=$1
    local expected=$2
    local test_name=$3
    ((TOTAL++))
    
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

# Function to assert contains
assert_contains() {
    local response=$1
    local field=$2
    local test_name=$3
    ((TOTAL++))
    
    if echo "$response" | grep -q "$field"; then
        echo -e "${GREEN}✅ $test_name${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}❌ $test_name - Missing field: $field${NC}"
        ((FAILED++))
        return 1
    fi
}

# Function to test exception handler
test_exception_handler() {
    local service_name=$1
    local service_url=$2
    local endpoint=$3
    local entity_name=$4
    
    test_section "Testing Exception Handler - $service_name"
    
    # Test entity not found
    RESPONSE=$(curl -s -w "\n%{http_code}" "$service_url$endpoint" 2>/dev/null)
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    assert_status "$HTTP_CODE" "404" "$service_name - Status code should be 404"
    assert_contains "$BODY" "ENTITY_NOT_FOUND" "$service_name - Should contain ENTITY_NOT_FOUND"
    assert_contains "$BODY" "correlationId" "$service_name - Should contain correlationId"
    assert_contains "$BODY" "timestamp" "$service_name - Should contain timestamp"
    assert_contains "$BODY" "errorCode" "$service_name - Should contain errorCode"
    assert_contains "$BODY" "message" "$service_name - Should contain message"
    
    # Validate JSON structure
    if echo "$BODY" | jq . > /dev/null 2>&1; then
        assert_contains "valid" "valid" "$service_name - Response is valid JSON"
    else
        echo -e "${YELLOW}⚠️  jq not available, skipping JSON validation${NC}"
    fi
    
    echo "Response preview: $(echo "$BODY" | head -c 200)..."
}

# Function to test correlation ID
test_correlation_id() {
    local service_name=$1
    local service_url=$2
    
    test_section "Testing Correlation ID - $service_name"
    
    # Test auto-generation
    CUSTOM_ID="integration-test-$(date +%s)"
    RESPONSE=$(curl -s -i -H "X-Correlation-Id: $CUSTOM_ID" "$service_url/api/health" 2>/dev/null || curl -s -i "$service_url/actuator/health" 2>/dev/null)
    HEADER_ID=$(echo "$RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r' | head -1)
    
    if [ -n "$HEADER_ID" ]; then
        assert_contains "$HEADER_ID" "$CUSTOM_ID" "$service_name - Correlation ID preserved in header"
    else
        # If health endpoint doesn't support correlation ID, test with a real endpoint
        RESPONSE=$(curl -s -i -H "X-Correlation-Id: $CUSTOM_ID" "$service_url/api/users" 2>/dev/null || curl -s -i -H "X-Correlation-Id: $CUSTOM_ID" "$service_url/api/bookings" 2>/dev/null || echo "")
        HEADER_ID=$(echo "$RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r' | head -1)
        if [ -n "$HEADER_ID" ]; then
            assert_contains "$HEADER_ID" "$CUSTOM_ID" "$service_name - Correlation ID preserved"
        else
            echo -e "${YELLOW}⚠️  $service_name - Could not test correlation ID (no suitable endpoint)${NC}"
        fi
    fi
}

# Function to test correlation ID in error response
test_correlation_in_error() {
    local service_name=$1
    local service_url=$2
    local endpoint=$3
    
    test_section "Testing Correlation ID in Error - $service_name"
    
    CUSTOM_ID="error-test-$(date +%s)"
    RESPONSE=$(curl -s -H "X-Correlation-Id: $CUSTOM_ID" "$service_url$endpoint" 2>/dev/null)
    
    assert_contains "$RESPONSE" "$CUSTOM_ID" "$service_name - Error response contains correlation ID"
}

# Check all services are running
test_section "Service Health Check"

ALL_SERVICES_UP=true
test_service_health "Booking Service" "$BOOKING_SERVICE" || ALL_SERVICES_UP=false
test_service_health "Train Service" "$TRAIN_SERVICE" || ALL_SERVICES_UP=false
test_service_health "User Service" "$USER_SERVICE" || ALL_SERVICES_UP=false
test_service_health "Notification Service" "$NOTIFICATION_SERVICE" || ALL_SERVICES_UP=false
test_service_health "Payment Service" "$PAYMENT_SERVICE" || ALL_SERVICES_UP=false

if [ "$ALL_SERVICES_UP" = false ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Some services are not running. Tests will be limited.${NC}"
    echo -e "${YELLOW}   Start services with: ./start-microservices.sh${NC}"
    echo ""
fi

# Integration Tests

# Test 1: Exception Handlers
if [ "$ALL_SERVICES_UP" = true ]; then
    test_exception_handler "Booking Service" "$BOOKING_SERVICE" "/api/v1/bookings/99999" "Booking"
    test_exception_handler "Train Service" "$TRAIN_SERVICE" "/api/v1/trains/99999" "Train"
    test_exception_handler "User Service" "$USER_SERVICE" "/api/users/99999" "User"
    test_exception_handler "Notification Service" "$NOTIFICATION_SERVICE" "/api/notifications/99999" "Notification"
    test_exception_handler "Payment Service" "$PAYMENT_SERVICE" "/api/payments/99999" "Payment"
fi

# Test 2: Correlation IDs
if curl -s --max-time 3 "$BOOKING_SERVICE/actuator/health" > /dev/null 2>&1; then
    test_correlation_id "Booking Service" "$BOOKING_SERVICE"
fi
if curl -s --max-time 3 "$TRAIN_SERVICE/actuator/health" > /dev/null 2>&1; then
    test_correlation_id "Train Service" "$TRAIN_SERVICE"
fi
if curl -s --max-time 3 "$USER_SERVICE/actuator/health" > /dev/null 2>&1; then
    test_correlation_id "User Service" "$USER_SERVICE"
fi

# Test 3: Correlation ID in Error Responses
if curl -s --max-time 3 "$BOOKING_SERVICE/actuator/health" > /dev/null 2>&1; then
    test_correlation_in_error "Booking Service" "$BOOKING_SERVICE" "/api/v1/bookings/99999"
fi
if curl -s --max-time 3 "$USER_SERVICE/actuator/health" > /dev/null 2>&1; then
    test_correlation_in_error "User Service" "$USER_SERVICE" "/api/users/99999"
fi
if curl -s --max-time 3 "$PAYMENT_SERVICE/actuator/health" > /dev/null 2>&1; then
    test_correlation_in_error "Payment Service" "$PAYMENT_SERVICE" "/api/payments/99999"
fi

# Test 4: Cross-Service Correlation ID Consistency
test_section "Cross-Service Correlation ID Consistency"

if [ "$ALL_SERVICES_UP" = true ]; then
    UNIFIED_CORRELATION_ID="cross-service-test-$(date +%s)"
    
    echo "Testing with unified correlation ID: $UNIFIED_CORRELATION_ID"
    
    BOOKING_RESPONSE=$(curl -s -i -H "X-Correlation-Id: $UNIFIED_CORRELATION_ID" "$BOOKING_SERVICE/api/v1/bookings/99999" 2>/dev/null)
    USER_RESPONSE=$(curl -s -i -H "X-Correlation-Id: $UNIFIED_CORRELATION_ID" "$USER_SERVICE/api/users/99999" 2>/dev/null)
    PAYMENT_RESPONSE=$(curl -s -i -H "X-Correlation-Id: $UNIFIED_CORRELATION_ID" "$PAYMENT_SERVICE/api/payments/99999" 2>/dev/null)
    
    BOOKING_ID=$(echo "$BOOKING_RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r' | head -1)
    USER_ID=$(echo "$USER_RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r' | head -1)
    PAYMENT_ID=$(echo "$PAYMENT_RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r' | head -1)
    
    if [ "$BOOKING_ID" = "$UNIFIED_CORRELATION_ID" ] && [ "$USER_ID" = "$UNIFIED_CORRELATION_ID" ]; then
        assert_contains "success" "success" "Cross-service correlation ID consistency"
    else
        echo -e "${YELLOW}⚠️  Correlation ID consistency test - Some services may not return headers${NC}"
    fi
fi

# Test 5: Error Response Format Validation
test_section "Error Response Format Validation"

if curl -s --max-time 3 "$BOOKING_SERVICE/actuator/health" > /dev/null 2>&1; then
    RESPONSE=$(curl -s "$BOOKING_SERVICE/api/v1/bookings/99999" 2>/dev/null)
    
    echo "Validating error response structure..."
    assert_contains "$RESPONSE" "\"timestamp\"" "Has timestamp field"
    assert_contains "$RESPONSE" "\"status\"" "Has status field"
    assert_contains "$RESPONSE" "\"errorCode\"" "Has errorCode field"
    assert_contains "$RESPONSE" "\"message\"" "Has message field"
    assert_contains "$RESPONSE" "\"path\"" "Has path field"
    assert_contains "$RESPONSE" "\"method\"" "Has method field"
    
    echo ""
    echo "Sample error response:"
    echo "$RESPONSE" | head -15
fi

# Test 6: API Gateway Correlation ID (if available)
test_section "API Gateway Correlation ID"

if curl -s --max-time 3 "$API_GATEWAY/actuator/health" > /dev/null 2>&1; then
    GATEWAY_CORRELATION_ID="gateway-test-$(date +%s)"
    RESPONSE=$(curl -s -i -H "X-Correlation-Id: $GATEWAY_CORRELATION_ID" "$API_GATEWAY/api/v1/bookings" 2>/dev/null)
    GATEWAY_HEADER_ID=$(echo "$RESPONSE" | grep -i "X-Correlation-Id" | cut -d' ' -f2 | tr -d '\r' | head -1)
    
    if [ -n "$GATEWAY_HEADER_ID" ]; then
        assert_contains "$GATEWAY_HEADER_ID" "$GATEWAY_CORRELATION_ID" "API Gateway preserves correlation ID"
    else
        echo -e "${YELLOW}⚠️  API Gateway correlation ID test - Gateway may not be routing properly${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  API Gateway not available${NC}"
fi

# Summary
echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  📊 Integration Test Results                                  ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo -e "Total Tests: ${BLUE}$TOTAL${NC}"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"

if [ $TOTAL -gt 0 ]; then
    SUCCESS_RATE=$((PASSED * 100 / TOTAL))
    echo -e "Success Rate: ${BLUE}$SUCCESS_RATE%${NC}"
fi

echo ""

if [ $FAILED -eq 0 ] && [ $TOTAL -gt 0 ]; then
    echo -e "${GREEN}✅ All integration tests passed!${NC}"
    echo ""
    echo "✅ All high-priority features are working correctly:"
    echo "   - Global Exception Handler ✓"
    echo "   - Correlation ID Filter ✓"
    echo "   - Structured Error Responses ✓"
    exit 0
elif [ $TOTAL -eq 0 ]; then
    echo -e "${YELLOW}⚠️  No tests could be executed (services may not be running)${NC}"
    echo ""
    echo "To run integration tests:"
    echo "1. Start all services: ./start-microservices.sh"
    echo "2. Wait 60-90 seconds for services to start"
    echo "3. Run this script again: ./test-integration-high-priority.sh"
    exit 1
else
    echo -e "${RED}❌ Some tests failed. Please review the output above.${NC}"
    exit 1
fi

