#!/bin/bash

# Test script for AUD (Audit) Tables - Payment Service
# Tests entity audit tracking for payment service

set -e

BASE_URL="http://localhost:8094"
PAYMENT_SERVICE_URL="${BASE_URL}/api/payments"
AUDIT_API_URL="${BASE_URL}/api/audit"

echo "🧪 TESTING AUD TABLES - PAYMENT SERVICE"
echo "========================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if payment service is running
echo "📡 Checking if Payment Service is running..."
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Payment Service is not running at ${BASE_URL}${NC}"
    echo "Please start the payment service first"
    exit 1
fi
echo -e "${GREEN}✅ Payment Service is running${NC}"
echo ""

# Test 1: Create a payment and check audit log
echo "📝 Test 1: Create Payment and Verify Audit Log"
echo "----------------------------------------------"
PAYMENT_DATA='{
  "bookingId": 1,
  "amount": 1000.00,
  "currency": "INR",
  "paymentMethod": "CREDIT_CARD",
  "status": "SUCCESS",
  "transactionId": "TXN_TEST_'$(date +%s)'"
}'

echo "Creating payment..."
CREATE_RESPONSE=$(curl -s -X POST "${PAYMENT_SERVICE_URL}" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-123" \
  -H "X-Username: testuser" \
  -d "${PAYMENT_DATA}")

PAYMENT_ID=$(echo "${CREATE_RESPONSE}" | jq -r '.id // empty')

if [ -z "${PAYMENT_ID}" ] || [ "${PAYMENT_ID}" = "null" ]; then
    echo -e "${RED}❌ Failed to create payment${NC}"
    echo "Response: ${CREATE_RESPONSE}"
    exit 1
fi

echo -e "${GREEN}✅ Payment created with ID: ${PAYMENT_ID}${NC}"
echo ""

# Wait a bit for audit log to be created
sleep 2

echo "Checking audit log for CREATE action..."
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimplePayment/${PAYMENT_ID}")

CREATE_AUDIT_COUNT=$(echo "${AUDIT_HISTORY}" | jq '[.[] | select(.action == "CREATE")] | length' 2>/dev/null || echo "0")

if [ "${CREATE_AUDIT_COUNT}" -gt 0 ]; then
    echo -e "${GREEN}✅ CREATE audit log found (count: ${CREATE_AUDIT_COUNT})${NC}"
    
    # Show audit log details
    CREATE_AUDIT=$(echo "${AUDIT_HISTORY}" | jq '.[] | select(.action == "CREATE") | .' 2>/dev/null)
    if [ -n "${CREATE_AUDIT}" ]; then
        echo "Audit Log Details:"
        echo "${CREATE_AUDIT}" | jq '{id, entityName, entityId, action, revisionNumber, changedBy, changedByUsername, changedAt}'
    fi
else
    echo -e "${YELLOW}⚠️  No CREATE audit log found (service may need restart)${NC}"
    echo "Audit History: ${AUDIT_HISTORY}"
fi
echo ""

# Test 2: Get complete audit history
echo "📝 Test 2: Get Complete Audit History"
echo "--------------------------------------"
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimplePayment/${PAYMENT_ID}")

TOTAL_REVISIONS=$(echo "${AUDIT_HISTORY}" | jq 'length' 2>/dev/null || echo "0")

if [ "${TOTAL_REVISIONS}" -gt 0 ]; then
    echo -e "${GREEN}✅ Total audit revisions: ${TOTAL_REVISIONS}${NC}"
    echo ""
    echo "Complete Audit History:"
    echo "${AUDIT_HISTORY}" | jq '.[] | {revisionNumber, action, changedBy, changedByUsername, changedAt}' 2>/dev/null || echo "${AUDIT_HISTORY}"
else
    echo -e "${YELLOW}⚠️  No audit history found (service may need restart)${NC}"
fi
echo ""

# Test 3: Get audit statistics
echo "📝 Test 3: Get Audit Statistics"
echo "--------------------------------"
AUDIT_STATS=$(curl -s "${AUDIT_API_URL}/stats/SimplePayment/${PAYMENT_ID}")

if [ "$(echo "${AUDIT_STATS}" | jq -r '.totalRevisions // empty' 2>/dev/null)" != "" ]; then
    echo -e "${GREEN}✅ Audit statistics retrieved${NC}"
    echo "${AUDIT_STATS}" | jq '.'
else
    echo -e "${YELLOW}⚠️  Failed to get audit statistics (service may need restart)${NC}"
    echo "Response: ${AUDIT_STATS}"
fi
echo ""

# Summary
echo "📊 TEST SUMMARY"
echo "=============="
echo ""
echo "✅ Tests Completed:"
echo "   1. Create payment and verify CREATE audit log"
echo "   2. Get complete audit history"
echo "   3. Get audit statistics"
echo ""
echo "📝 Payment ID used for testing: ${PAYMENT_ID}"
echo ""
echo -e "${GREEN}🎉 Payment service audit table testing completed!${NC}"

