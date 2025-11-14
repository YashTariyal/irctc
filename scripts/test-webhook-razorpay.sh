#!/bin/bash

# Razorpay Webhook Test Script
# This script tests the Razorpay webhook endpoint

set -e

# Configuration
WEBHOOK_URL="${WEBHOOK_URL:-http://localhost:8083/api/payments/webhooks/razorpay}"
WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:-test_secret}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing Razorpay Webhook Endpoint${NC}"
echo "Webhook URL: $WEBHOOK_URL"
echo ""

# Generate test payload
PAYLOAD='{
  "event": "payment.captured",
  "payload": {
    "payment": {
      "entity": {
        "id": "pay_test_'$(date +%s)'",
        "amount": 100000,
        "currency": "INR",
        "status": "captured",
        "order_id": "order_test_'$(date +%s)'",
        "created_at": '$(date +%s)'
      }
    }
  }
}'

# Generate signature (simplified - in production, use proper HMAC)
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" | cut -d' ' -f2)

echo -e "${YELLOW}Sending test webhook...${NC}"
echo "Payload: $PAYLOAD"
echo ""

# Send webhook
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: $SIGNATURE" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status Code: $HTTP_CODE"
echo "Response: $BODY"
echo ""

# Check response
if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ Webhook test successful!${NC}"
    exit 0
else
    echo -e "${RED}❌ Webhook test failed!${NC}"
    exit 1
fi

