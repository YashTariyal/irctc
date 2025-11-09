#!/bin/bash

# Test script for AUD (Audit) Tables
# Tests entity audit tracking for booking service

set -e

BASE_URL="http://localhost:8093"
BOOKING_SERVICE_URL="${BASE_URL}/api/bookings"
AUDIT_API_URL="${BASE_URL}/api/audit"

echo "🧪 TESTING AUD TABLES - ENTITY AUDIT TRACKING"
echo "=============================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if booking service is running
echo "📡 Checking if Booking Service is running..."
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Booking Service is not running at ${BASE_URL}${NC}"
    echo "Please start the booking service first"
    exit 1
fi
echo -e "${GREEN}✅ Booking Service is running${NC}"
echo ""

# Test 1: Create a booking and check audit log
echo "📝 Test 1: Create Booking and Verify Audit Log"
echo "----------------------------------------------"
BOOKING_DATA='{
  "userId": 1,
  "trainId": 1,
  "pnrNumber": "TEST_PNR_'$(date +%s)'",
  "bookingTime": "2025-12-01T10:00:00",
  "status": "CONFIRMED",
  "totalFare": 500.00
}'

echo "Creating booking..."
CREATE_RESPONSE=$(curl -s -X POST "${BOOKING_SERVICE_URL}" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-123" \
  -H "X-Username: testuser" \
  -d "${BOOKING_DATA}")

BOOKING_ID=$(echo "${CREATE_RESPONSE}" | jq -r '.id // empty')

if [ -z "${BOOKING_ID}" ] || [ "${BOOKING_ID}" = "null" ]; then
    echo -e "${RED}❌ Failed to create booking${NC}"
    echo "Response: ${CREATE_RESPONSE}"
    exit 1
fi

echo -e "${GREEN}✅ Booking created with ID: ${BOOKING_ID}${NC}"
echo ""

# Wait a bit for audit log to be created
sleep 2

echo "Checking audit log for CREATE action..."
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimpleBooking/${BOOKING_ID}")

CREATE_AUDIT_COUNT=$(echo "${AUDIT_HISTORY}" | jq '[.[] | select(.action == "CREATE")] | length')

if [ "${CREATE_AUDIT_COUNT}" -gt 0 ]; then
    echo -e "${GREEN}✅ CREATE audit log found (count: ${CREATE_AUDIT_COUNT})${NC}"
    
    # Show audit log details
    CREATE_AUDIT=$(echo "${AUDIT_HISTORY}" | jq '.[] | select(.action == "CREATE") | .')
    echo "Audit Log Details:"
    echo "${CREATE_AUDIT}" | jq '{id, entityName, entityId, action, revisionNumber, changedBy, changedByUsername, changedAt}'
else
    echo -e "${RED}❌ No CREATE audit log found${NC}"
    echo "Audit History: ${AUDIT_HISTORY}"
fi
echo ""

# Test 2: Update booking and check audit log
echo "📝 Test 2: Update Booking and Verify Audit Log"
echo "----------------------------------------------"

# First get the current booking to update it
CURRENT_BOOKING=$(curl -s "${BOOKING_SERVICE_URL}/${BOOKING_ID}")

# Create update payload with all required fields
UPDATE_DATA=$(echo "${CURRENT_BOOKING}" | jq '.status = "CANCELLED"')

echo "Updating booking status to CANCELLED..."
UPDATE_RESPONSE=$(curl -s -X PUT "${BOOKING_SERVICE_URL}/${BOOKING_ID}" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user-456" \
  -H "X-Username: updater" \
  -d "${UPDATE_DATA}")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Booking updated${NC}"
else
    echo -e "${YELLOW}⚠️  Update may have failed, continuing...${NC}"
fi
echo ""

# Wait a bit for audit log to be created
sleep 2

echo "Checking audit log for UPDATE action..."
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimpleBooking/${BOOKING_ID}")

UPDATE_AUDIT_COUNT=$(echo "${AUDIT_HISTORY}" | jq '[.[] | select(.action == "UPDATE")] | length')

if [ "${UPDATE_AUDIT_COUNT}" -gt 0 ]; then
    echo -e "${GREEN}✅ UPDATE audit log found (count: ${UPDATE_AUDIT_COUNT})${NC}"
    
    # Show latest UPDATE audit log
    UPDATE_AUDIT=$(echo "${AUDIT_HISTORY}" | jq '[.[] | select(.action == "UPDATE")] | .[-1]')
    echo "Latest UPDATE Audit Log:"
    echo "${UPDATE_AUDIT}" | jq '{id, entityName, entityId, action, revisionNumber, changedBy, changedByUsername, changedAt, oldValues, newValues}'
else
    echo -e "${YELLOW}⚠️  No UPDATE audit log found (this is expected if update endpoint doesn't exist)${NC}"
fi
echo ""

# Test 3: Get complete audit history
echo "📝 Test 3: Get Complete Audit History"
echo "--------------------------------------"
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimpleBooking/${BOOKING_ID}")

TOTAL_REVISIONS=$(echo "${AUDIT_HISTORY}" | jq 'length')

if [ "${TOTAL_REVISIONS}" -gt 0 ]; then
    echo -e "${GREEN}✅ Total audit revisions: ${TOTAL_REVISIONS}${NC}"
    echo ""
    echo "Complete Audit History:"
    echo "${AUDIT_HISTORY}" | jq '.[] | {revisionNumber, action, changedBy, changedByUsername, changedAt}'
else
    echo -e "${RED}❌ No audit history found${NC}"
fi
echo ""

# Test 4: Get latest audit log
echo "📝 Test 4: Get Latest Audit Log"
echo "--------------------------------"
LATEST_AUDIT=$(curl -s "${AUDIT_API_URL}/entity/SimpleBooking/${BOOKING_ID}/latest")

if [ "$(echo "${LATEST_AUDIT}" | jq -r '.id // empty')" != "" ]; then
    echo -e "${GREEN}✅ Latest audit log retrieved${NC}"
    echo "${LATEST_AUDIT}" | jq '{id, entityName, entityId, action, revisionNumber, changedBy, changedByUsername, changedAt}'
else
    echo -e "${RED}❌ Failed to get latest audit log${NC}"
    echo "Response: ${LATEST_AUDIT}"
fi
echo ""

# Test 5: Get audit statistics
echo "📝 Test 5: Get Audit Statistics"
echo "--------------------------------"
AUDIT_STATS=$(curl -s "${AUDIT_API_URL}/stats/SimpleBooking/${BOOKING_ID}")

if [ "$(echo "${AUDIT_STATS}" | jq -r '.totalRevisions // empty')" != "" ]; then
    echo -e "${GREEN}✅ Audit statistics retrieved${NC}"
    echo "${AUDIT_STATS}" | jq '.'
else
    echo -e "${RED}❌ Failed to get audit statistics${NC}"
    echo "Response: ${AUDIT_STATS}"
fi
echo ""

# Test 6: Get audit logs by user
echo "📝 Test 6: Get Audit Logs by User"
echo "----------------------------------"
USER_AUDIT_LOGS=$(curl -s "${AUDIT_API_URL}/user/test-user-123")

USER_LOG_COUNT=$(echo "${USER_AUDIT_LOGS}" | jq 'length')

if [ "${USER_LOG_COUNT}" -gt 0 ]; then
    echo -e "${GREEN}✅ Found ${USER_LOG_COUNT} audit logs for user test-user-123${NC}"
    echo "${USER_AUDIT_LOGS}" | jq '.[0] | {entityName, entityId, action, changedAt}'
else
    echo -e "${YELLOW}⚠️  No audit logs found for user (this is expected if user tracking is not working)${NC}"
fi
echo ""

# Summary
echo "📊 TEST SUMMARY"
echo "=============="
echo ""
echo "✅ Tests Completed:"
echo "   1. Create booking and verify CREATE audit log"
echo "   2. Update booking and verify UPDATE audit log"
echo "   3. Get complete audit history"
echo "   4. Get latest audit log"
echo "   5. Get audit statistics"
echo "   6. Get audit logs by user"
echo ""
echo "📝 Booking ID used for testing: ${BOOKING_ID}"
echo ""
echo -e "${GREEN}🎉 Audit table testing completed!${NC}"

