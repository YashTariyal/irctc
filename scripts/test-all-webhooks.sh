#!/bin/bash

# Comprehensive Webhook Testing Script
# Tests all webhook endpoints with proper signatures

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
echo -e "${BLUE}Comprehensive Webhook Testing${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Base URL: $BASE_URL"
echo ""

# Load .env if exists
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Test Razorpay Webhook
echo -e "${BLUE}Testing Razorpay Webhook...${NC}"
if [ -z "$RAZORPAY_WEBHOOK_SECRET" ]; then
    echo -e "${YELLOW}⚠️  RAZORPAY_WEBHOOK_SECRET not set, using test mode${NC}"
    RAZORPAY_WEBHOOK_SECRET="test_secret"
fi

if ./scripts/test-webhook-razorpay.sh; then
    echo -e "${GREEN}✅ Razorpay webhook test passed${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Razorpay webhook test failed${NC}"
    ((FAILED++))
fi
echo ""

# Test Stripe Webhook
echo -e "${BLUE}Testing Stripe Webhook...${NC}"
if [ -z "$STRIPE_WEBHOOK_SECRET" ]; then
    echo -e "${YELLOW}⚠️  STRIPE_WEBHOOK_SECRET not set, using test mode${NC}"
    STRIPE_WEBHOOK_SECRET="test_secret"
fi

if ./scripts/test-webhook-stripe.sh; then
    echo -e "${GREEN}✅ Stripe webhook test passed${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ Stripe webhook test failed${NC}"
    ((FAILED++))
fi
echo ""

# Test PayU Webhook
echo -e "${BLUE}Testing PayU Webhook...${NC}"
if [ -z "$PAYU_MERCHANT_SALT" ]; then
    echo -e "${YELLOW}⚠️  PAYU_MERCHANT_SALT not set, using test mode${NC}"
    PAYU_MERCHANT_SALT="test_salt"
fi

if ./scripts/test-webhook-payu.sh; then
    echo -e "${GREEN}✅ PayU webhook test passed${NC}"
    ((PASSED++))
else
    echo -e "${RED}❌ PayU webhook test failed${NC}"
    ((FAILED++))
fi
echo ""

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Webhook Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All webhook tests passed!${NC}"
    echo ""
    echo -e "${YELLOW}Next Steps:${NC}"
    echo "1. Configure webhook URLs in gateway dashboards"
    echo "2. Test with real webhook events from gateways"
    echo "3. Monitor webhook processing logs"
    exit 0
else
    echo -e "${RED}❌ Some webhook tests failed${NC}"
    echo ""
    echo -e "${YELLOW}Troubleshooting:${NC}"
    echo "1. Check if service is running: curl $BASE_URL/actuator/health"
    echo "2. Check webhook endpoint logs"
    echo "3. Verify webhook secrets are correct"
    exit 1
fi

