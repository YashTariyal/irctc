#!/bin/bash

# Diagnostic test for AUD tables
# Checks if audit infrastructure is properly set up

set -e

BASE_URL="http://localhost:8093"
echo "🔍 AUD TABLES DIAGNOSTIC TEST"
echo "=============================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check 1: Service health
echo "1️⃣  Checking Booking Service Health..."
if curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Service is running${NC}"
    HEALTH=$(curl -s "${BASE_URL}/actuator/health" | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
    echo "   Status: ${HEALTH}"
else
    echo -e "${RED}❌ Service is not running${NC}"
    exit 1
fi
echo ""

# Check 2: Audit endpoint availability
echo "2️⃣  Checking Audit API Endpoint..."
AUDIT_RESPONSE=$(curl -s "${BASE_URL}/api/audit/entity/SimpleBooking/1" 2>&1)
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/audit/entity/SimpleBooking/1" 2>&1)
AUDIT_BODY="${AUDIT_RESPONSE}"

if [ "${HTTP_CODE}" = "200" ]; then
    echo -e "${GREEN}✅ Audit API is accessible${NC}"
    echo "   Response: ${AUDIT_BODY}" | jq 'length' 2>/dev/null || echo "   Response: ${AUDIT_BODY}"
elif [ "${HTTP_CODE}" = "500" ]; then
    echo -e "${RED}❌ Audit API returns 500 error${NC}"
    echo "   This usually means:"
    echo "   - Database table doesn't exist (migration not run)"
    echo "   - EntityAuditService/Repository not initialized"
    echo "   - Service needs restart after code changes"
    echo ""
    echo "   Error details:"
    echo "${AUDIT_BODY}" | jq '.' 2>/dev/null || echo "${AUDIT_BODY}"
elif [ "${HTTP_CODE}" = "404" ]; then
    echo -e "${YELLOW}⚠️  Audit API endpoint not found${NC}"
    echo "   Controller might not be registered"
else
    echo -e "${YELLOW}⚠️  Audit API returned HTTP ${HTTP_CODE}${NC}"
    echo "   Response: ${AUDIT_BODY}"
fi
echo ""

# Check 3: Create a test booking and check if audit log is created
echo "3️⃣  Testing Audit Log Creation..."
echo "   Creating a test booking..."
BOOKING_DATA='{
  "userId": 1,
  "trainId": 1,
  "pnrNumber": "DIAG_TEST_'$(date +%s)'",
  "bookingTime": "2025-12-01T10:00:00",
  "status": "CONFIRMED",
  "totalFare": 600.00
}'

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/bookings" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: diagnostic-test-user" \
  -H "X-Username: diagnostictester" \
  -d "${BOOKING_DATA}")

BOOKING_ID=$(echo "${CREATE_RESPONSE}" | jq -r '.id // empty' 2>/dev/null)

if [ -z "${BOOKING_ID}" ] || [ "${BOOKING_ID}" = "null" ]; then
    echo -e "${RED}❌ Failed to create booking${NC}"
    echo "   Response: ${CREATE_RESPONSE}"
else
    echo -e "${GREEN}✅ Booking created: ID ${BOOKING_ID}${NC}"
    
    # Wait for audit log
    sleep 3
    
    echo "   Checking for audit log..."
    AUDIT_CHECK=$(curl -s "${BASE_URL}/api/audit/entity/SimpleBooking/${BOOKING_ID}" 2>&1)
    
    if echo "${AUDIT_CHECK}" | jq -e '. | length > 0' > /dev/null 2>&1; then
        AUDIT_COUNT=$(echo "${AUDIT_CHECK}" | jq 'length')
        echo -e "${GREEN}✅ Audit log found! (${AUDIT_COUNT} entries)${NC}"
        
        # Show first audit log
        echo "   First audit log:"
        echo "${AUDIT_CHECK}" | jq '.[0] | {action, revisionNumber, changedBy, changedByUsername, changedAt}' 2>/dev/null
    elif echo "${AUDIT_CHECK}" | grep -q "500\|error" 2>/dev/null; then
        echo -e "${RED}❌ Audit API error when querying${NC}"
        echo "   This confirms the audit infrastructure has issues"
    else
        echo -e "${YELLOW}⚠️  No audit log found${NC}"
        echo "   This could mean:"
        echo "   - EntityAuditListener is not working"
        echo "   - Database table doesn't exist"
        echo "   - Service needs restart"
    fi
fi
echo ""

# Check 4: Database migration status
echo "4️⃣  Checking Database Migration Status..."
FLYWAY_RESPONSE=$(curl -s "${BASE_URL}/actuator/flyway" 2>&1)

if echo "${FLYWAY_RESPONSE}" | jq -e '.contexts' > /dev/null 2>&1; then
    MIGRATION=$(echo "${FLYWAY_RESPONSE}" | jq '.contexts.application.flywayBeans.flyway.migrations[] | select(.script | contains("entity_audit"))' 2>/dev/null)
    
    if [ -n "${MIGRATION}" ]; then
        echo -e "${GREEN}✅ Migration V6 found${NC}"
        echo "${MIGRATION}" | jq '{script, installedOn, state}' 2>/dev/null
    else
        echo -e "${YELLOW}⚠️  Migration V6 not found in Flyway history${NC}"
        echo "   Migration might not have run"
    fi
else
    echo -e "${YELLOW}⚠️  Flyway endpoint not available${NC}"
    echo "   Cannot check migration status"
fi
echo ""

# Summary and recommendations
echo "📊 DIAGNOSTIC SUMMARY"
echo "===================="
echo ""
echo "Based on the tests above:"
echo ""
echo "✅ If audit API works:"
echo "   - Audit infrastructure is properly set up"
echo "   - You can proceed with full testing"
echo ""
echo "❌ If audit API returns 500:"
echo "   - Service needs to be RESTARTED to load new code"
echo "   - Database migration V6 needs to run"
echo "   - Check service logs for detailed errors"
echo ""
echo "🔧 Recommended Actions:"
echo "   1. Restart the booking service"
echo "   2. Wait for service to fully start"
echo "   3. Run this diagnostic again"
echo "   4. If still failing, check service logs"
echo ""

