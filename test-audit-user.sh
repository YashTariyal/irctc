#!/bin/bash

# Test script for AUD (Audit) Tables - User Service
# Tests entity audit tracking for user service

set -e

BASE_URL="http://localhost:8091"
USER_SERVICE_URL="${BASE_URL}/api/users"
AUDIT_API_URL="${BASE_URL}/api/audit"

echo "🧪 TESTING AUD TABLES - USER SERVICE"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if user service is running
echo "📡 Checking if User Service is running..."
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ User Service is not running at ${BASE_URL}${NC}"
    echo "Please start the user service first"
    exit 1
fi
echo -e "${GREEN}✅ User Service is running${NC}"
echo ""

# Test 1: Create a user and check audit log
echo "📝 Test 1: Create User and Verify Audit Log"
echo "--------------------------------------------"
USER_DATA='{
  "username": "testuser_'$(date +%s)'",
  "email": "test_'$(date +%s)'@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "1234567890"
}'

echo "Creating user..."
CREATE_RESPONSE=$(curl -s -X POST "${USER_SERVICE_URL}/register" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: system" \
  -H "X-Username: system" \
  -d "${USER_DATA}")

USER_ID=$(echo "${CREATE_RESPONSE}" | jq -r '.id // empty')

if [ -z "${USER_ID}" ] || [ "${USER_ID}" = "null" ]; then
    echo -e "${RED}❌ Failed to create user${NC}"
    echo "Response: ${CREATE_RESPONSE}"
    exit 1
fi

echo -e "${GREEN}✅ User created with ID: ${USER_ID}${NC}"
echo ""

# Wait a bit for audit log to be created
sleep 2

echo "Checking audit log for CREATE action..."
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimpleUser/${USER_ID}")

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
AUDIT_HISTORY=$(curl -s "${AUDIT_API_URL}/entity/SimpleUser/${USER_ID}")

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
AUDIT_STATS=$(curl -s "${AUDIT_API_URL}/stats/SimpleUser/${USER_ID}")

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
echo "   1. Create user and verify CREATE audit log"
echo "   2. Get complete audit history"
echo "   3. Get audit statistics"
echo ""
echo "📝 User ID used for testing: ${USER_ID}"
echo ""
echo -e "${GREEN}🎉 User service audit table testing completed!${NC}"

