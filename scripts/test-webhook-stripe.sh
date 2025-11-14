#!/bin/bash

# Stripe Webhook Test Script
# This script tests the Stripe webhook endpoint

set -e

# Configuration
WEBHOOK_URL="${WEBHOOK_URL:-http://localhost:8083/api/payments/webhooks/stripe}"
WEBHOOK_SECRET="${STRIPE_WEBHOOK_SECRET:-test_secret}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing Stripe Webhook Endpoint${NC}"
echo "Webhook URL: $WEBHOOK_URL"
echo ""

# Generate test payload
TIMESTAMP=$(date +%s)
PAYLOAD='{
  "id": "evt_test_'$(date +%s)'",
  "object": "event",
  "type": "payment_intent.succeeded",
  "data": {
    "object": {
      "id": "pi_test_'$(date +%s)'",
      "object": "payment_intent",
      "amount": 100000,
      "currency": "inr",
      "status": "succeeded"
    }
  },
  "created": '$TIMESTAMP'
}'

# Generate signature (simplified - in production, use proper HMAC)
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" | cut -d' ' -f2)
SIGNATURE_HEADER="$TIMESTAMP,$SIGNATURE"

echo -e "${YELLOW}Sending test webhook...${NC}"
echo "Payload: $PAYLOAD"
echo ""

# Send webhook
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -H "Stripe-Signature: $SIGNATURE_HEADER" \
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

