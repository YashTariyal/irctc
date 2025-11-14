#!/bin/bash

# Post-Deployment Testing Script
# Tests all endpoints after deployment

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="${BASE_URL:-http://localhost:8083}"
PASSED=0
FAILED=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Post-Deployment Testing${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Base URL: $BASE_URL"
echo ""

# Test function
test_endpoint() {
    local name=$1
    local url=$2
    local method=${3:-GET}
    local data=${4:-""}
    
    echo -e "${YELLOW}Testing: $name${NC}"
    
    if [ "$method" = "GET" ]; then
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    else
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" "$url")
    fi
    
    if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
        echo -e "${GREEN}✅ $name: HTTP $HTTP_CODE${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}❌ $name: HTTP $HTTP_CODE${NC}"
        ((FAILED++))
        return 1
    fi
}

# Health Check
test_endpoint "Health Check" "$BASE_URL/actuator/health"

# Metrics
test_endpoint "Metrics" "$BASE_URL/actuator/metrics"

# Gateway Endpoints
test_endpoint "List Gateways" "$BASE_URL/api/payments/gateways"
test_endpoint "Gateway Statistics" "$BASE_URL/api/payments/gateways/stats"

# Analytics Endpoints
test_endpoint "Analytics Overview" "$BASE_URL/api/payments/analytics/overview"
test_endpoint "Daily Analytics" "$BASE_URL/api/payments/analytics/daily"
test_endpoint "Gateway Performance" "$BASE_URL/api/payments/analytics/gateway-performance"
test_endpoint "Payment Methods" "$BASE_URL/api/payments/analytics/payment-methods"

# Webhook Endpoints (should return 400/405 for GET, which is expected)
echo -e "${YELLOW}Testing webhook endpoints (expected: 405 Method Not Allowed for GET)...${NC}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/payments/webhooks/razorpay")
if [ "$HTTP_CODE" -eq 405 ] || [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "${GREEN}✅ Razorpay Webhook: HTTP $HTTP_CODE (endpoint exists)${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Razorpay Webhook: HTTP $HTTP_CODE${NC}"
    ((FAILED++))
fi

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/payments/webhooks/stripe")
if [ "$HTTP_CODE" -eq 405 ] || [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "${GREEN}✅ Stripe Webhook: HTTP $HTTP_CODE (endpoint exists)${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Stripe Webhook: HTTP $HTTP_CODE${NC}"
    ((FAILED++))
fi

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/payments/webhooks/payu")
if [ "$HTTP_CODE" -eq 405 ] || [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "${GREEN}✅ PayU Webhook: HTTP $HTTP_CODE (endpoint exists)${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ PayU Webhook: HTTP $HTTP_CODE${NC}"
    ((FAILED++))
fi

# Payment Processing (test with minimal data)
echo ""
echo -e "${YELLOW}Testing payment processing...${NC}"
PAYMENT_DATA='{"bookingId":999,"amount":100.00,"currency":"INR","paymentMethod":"CARD"}'
test_endpoint "Process Payment" "$BASE_URL/api/payments" "POST" "$PAYMENT_DATA"

# Summary
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed${NC}"
    exit 1
fi

