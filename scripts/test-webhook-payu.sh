#!/bin/bash

# PayU Webhook Test Script
# This script tests the PayU webhook endpoint

set -e

# Configuration
WEBHOOK_URL="${WEBHOOK_URL:-http://localhost:8083/api/payments/webhooks/payu}"
MERCHANT_SALT="${PAYU_MERCHANT_SALT:-test_salt}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing PayU Webhook Endpoint${NC}"
echo "Webhook URL: $WEBHOOK_URL"
echo ""

# Generate test data
TXNID="TXN_test_$(date +%s)"
AMOUNT="1000.00"
PRODUCTINFO="Test Product"
FIRSTNAME="Test"
EMAIL="test@example.com"
STATUS="success"

# Generate hash
HASH_STRING="$MERCHANT_SALT|$STATUS|||||||||||$EMAIL|$FIRSTNAME|$PRODUCTINFO|$AMOUNT|$TXNID"
HASH=$(echo -n "$HASH_STRING" | openssl dgst -sha512 | cut -d' ' -f2)

# Generate payload
PAYLOAD="{
  \"status\": \"$STATUS\",
  \"txnid\": \"$TXNID\",
  \"amount\": \"$AMOUNT\",
  \"productinfo\": \"$PRODUCTINFO\",
  \"firstname\": \"$FIRSTNAME\",
  \"email\": \"$EMAIL\",
  \"hash\": \"$HASH\"
}"

echo -e "${YELLOW}Sending test webhook...${NC}"
echo "Payload: $PAYLOAD"
echo ""

# Send webhook
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
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

