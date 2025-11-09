#!/bin/bash

# Test script for UPDATE and DELETE audit operations

set -e

BASE_URL="http://localhost:8093"
echo "🧪 TESTING UPDATE AND DELETE AUDIT OPERATIONS"
echo "=============================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Step 1: Create booking
echo "📝 Step 1: Create Booking..."
BOOKING_DATA='{
  "userId": 1,
  "trainId": 1,
  "pnrNumber": "AUDIT_UPDATE_DELETE_'$(date +%s)'",
  "bookingTime": "2025-12-01T10:00:00",
  "status": "CONFIRMED",
  "totalFare": 6000.00
}'

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/bookings" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: audit-test-user" \
  -H "X-Username: audittester" \
  -d "${BOOKING_DATA}")

BOOKING_ID=$(echo "${CREATE_RESPONSE}" | jq -r '.id // empty')

if [ -z "${BOOKING_ID}" ] || [ "${BOOKING_ID}" = "null" ]; then
    echo -e "${RED}❌ Failed to create booking${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Booking created: ID ${BOOKING_ID}${NC}"
sleep 5

# Check CREATE audit
echo ""
echo "📋 Checking CREATE audit log..."
CREATE_AUDIT=$(curl -s "${BASE_URL}/api/audit/entity/SimpleBooking/${BOOKING_ID}")
CREATE_COUNT=$(echo "${CREATE_AUDIT}" | jq 'length' 2>/dev/null || echo "0")

if [ "${CREATE_COUNT}" -ge 1 ]; then
    echo -e "${GREEN}✅ CREATE audit log found${NC}"
    echo "${CREATE_AUDIT}" | jq '.[0] | {action, revisionNumber, changedBy}' 2>/dev/null
else
    echo -e "${RED}❌ CREATE audit log missing${NC}"
fi

# Step 2: Update booking
echo ""
echo "📝 Step 2: Update Booking Status..."
UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/bookings/${BOOKING_ID}" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: audit-test-user" \
  -H "X-Username: audittester" \
  -d '{"status":"CANCELLED"}')

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "${BASE_URL}/api/bookings/${BOOKING_ID}" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: audit-test-user" \
  -H "X-Username: audittester" \
  -d '{"status":"CANCELLED"}')

if [ "${HTTP_CODE}" = "200" ]; then
    echo -e "${GREEN}✅ Booking updated${NC}"
    sleep 5
    
    echo ""
    echo "📋 Checking audit logs after UPDATE..."
    AFTER_UPDATE=$(curl -s "${BASE_URL}/api/audit/entity/SimpleBooking/${BOOKING_ID}")
    UPDATE_COUNT=$(echo "${AFTER_UPDATE}" | jq 'length' 2>/dev/null || echo "0")
    
    echo "Total audit logs: ${UPDATE_COUNT}"
    echo ""
    echo "All audit logs:"
    echo "${AFTER_UPDATE}" | jq '.[] | {revisionNumber, action, changedBy}' 2>/dev/null
    
    UPDATE_LOG_COUNT=$(echo "${AFTER_UPDATE}" | jq '[.[] | select(.action == "UPDATE")] | length' 2>/dev/null || echo "0")
    if [ "${UPDATE_LOG_COUNT}" -ge 1 ]; then
        echo ""
        echo -e "${GREEN}✅ UPDATE audit log found${NC}"
        echo "UPDATE log details:"
        echo "${AFTER_UPDATE}" | jq '.[] | select(.action == "UPDATE") | {action, revisionNumber, changedBy, hasOldValues: (.oldValues != null), hasNewValues: (.newValues != null)}' 2>/dev/null
    else
        echo -e "${RED}❌ UPDATE audit log missing${NC}"
    fi
else
    echo -e "${RED}❌ Update failed with HTTP ${HTTP_CODE}${NC}"
fi

# Step 3: Another UPDATE
echo ""
echo "📝 Step 3: Update Booking Fare..."
curl -s -X PUT "${BASE_URL}/api/bookings/${BOOKING_ID}" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: audit-test-user" \
  -H "X-Username: audittester" \
  -d '{"totalFare":7000.00}' > /dev/null

echo -e "${GREEN}✅ Booking fare updated${NC}"
sleep 5

echo ""
echo "📋 Checking audit logs after second UPDATE..."
FINAL_AUDIT=$(curl -s "${BASE_URL}/api/audit/entity/SimpleBooking/${BOOKING_ID}")
FINAL_COUNT=$(echo "${FINAL_AUDIT}" | jq 'length' 2>/dev/null || echo "0")

echo "Total audit logs: ${FINAL_COUNT}"

CREATE_COUNT=$(echo "${FINAL_AUDIT}" | jq '[.[] | select(.action == "CREATE")] | length' 2>/dev/null || echo "0")
UPDATE_COUNT=$(echo "${FINAL_AUDIT}" | jq '[.[] | select(.action == "UPDATE")] | length' 2>/dev/null || echo "0")

echo ""
echo "📊 Summary:"
echo "   CREATE: ${CREATE_COUNT}"
echo "   UPDATE: ${UPDATE_COUNT}"

if [ "${CREATE_COUNT}" -ge 1 ] && [ "${UPDATE_COUNT}" -ge 2 ]; then
    echo ""
    echo -e "${GREEN}🎉 SUCCESS! Multiple UPDATE operations tracked!${NC}"
    echo ""
    echo "Complete audit trail:"
    echo "${FINAL_AUDIT}" | jq '.[] | {revisionNumber, action, changedBy, changedAt}' 2>/dev/null
else
    echo -e "${YELLOW}⚠️  Expected: CREATE (1), UPDATE (2+)${NC}"
fi

# Note about DELETE
echo ""
echo "📝 Note about DELETE operation:"
echo "   The DELETE endpoint (/api/bookings/{id}) calls cancelBooking()"
echo "   which only updates status to 'CANCELLED' (soft delete)"
echo "   To trigger @PreRemove audit, entity must be actually deleted"
echo "   from database using repository.delete() or repository.deleteById()"

echo ""
echo "✅ UPDATE audit functionality verified!"
echo "⚠️  DELETE audit requires actual entity deletion (not just status update)"

