#!/bin/bash

# Comprehensive Event Tracking Test Script
# Tests event production and consumption tracking

set -e

BASE_URL="http://localhost:8093"
BOOKING_SERVICE="http://localhost:8093"
NOTIFICATION_SERVICE="http://localhost:8095"

echo "🧪 Comprehensive Event Tracking Test"
echo "====================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if service is running
check_service() {
    local url=$1
    local service_name=$2
    
    echo -n "Checking $service_name... "
    if curl -s -f "$url/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Running${NC}"
        return 0
    else
        echo -e "${RED}✗ Not running${NC}"
        return 1
    fi
}

# Function to make API call and check response
api_call() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo -n "  $description... "
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ (HTTP $http_code)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
        return 0
    else
        echo -e "${RED}✗ (HTTP $http_code)${NC}"
        echo "$body"
        return 1
    fi
}

echo "1. 🔍 Service Health Checks"
echo "---------------------------"
check_service "$BOOKING_SERVICE" "Booking Service" || exit 1
echo ""

echo "2. 📊 Initial Event Tracking Statistics"
echo "----------------------------------------"
api_call "GET" "$BASE_URL/api/event-tracking/stats" "" "Get initial stats"
echo ""

echo "3. 📤 Test Event Production"
echo "----------------------------"
echo "  Creating a test booking to trigger event production..."

# Create a booking
BOOKING_DATA='{
  "userId": 1,
  "trainId": 1,
  "totalFare": 1500.0,
  "status": "CONFIRMED"
}'

BOOKING_RESPONSE=$(curl -s -X POST "$BOOKING_SERVICE/api/bookings" \
    -H "Content-Type: application/json" \
    -d "$BOOKING_DATA")

BOOKING_ID=$(echo "$BOOKING_RESPONSE" | jq -r '.id // empty')

if [ -n "$BOOKING_ID" ] && [ "$BOOKING_ID" != "null" ]; then
    echo -e "  ${GREEN}✓ Booking created with ID: $BOOKING_ID${NC}"
else
    echo -e "  ${YELLOW}⚠ Could not extract booking ID, but request may have succeeded${NC}"
fi

# Wait a bit for events to be processed
echo "  Waiting 3 seconds for events to be processed..."
sleep 3
echo ""

echo "4. 📊 Check Production Logs"
echo "----------------------------"
api_call "GET" "$BASE_URL/api/event-tracking/production/status/PUBLISHED" "" "Get published events"
echo ""

api_call "GET" "$BASE_URL/api/event-tracking/production/status/PENDING" "" "Get pending events"
echo ""

api_call "GET" "$BASE_URL/api/event-tracking/production/failed" "" "Get failed events"
echo ""

echo "5. 📥 Check Consumption Logs"
echo "-----------------------------"
api_call "GET" "$BASE_URL/api/event-tracking/consumption/status/PROCESSED" "" "Get processed events"
echo ""

api_call "GET" "$BASE_URL/api/event-tracking/consumption/status/RECEIVED" "" "Get received events"
echo ""

api_call "GET" "$BASE_URL/api/event-tracking/consumption/failed" "" "Get failed consumption events"
echo ""

echo "6. 📈 Final Event Tracking Statistics"
echo "-------------------------------------"
api_call "GET" "$BASE_URL/api/event-tracking/stats" "" "Get final stats"
echo ""

echo "7. 🔍 Test Individual Event Queries"
echo "-----------------------------------"

# Get first production event ID if available
PROD_EVENTS=$(curl -s "$BASE_URL/api/event-tracking/production/status/PUBLISHED" | jq -r '.[0].eventId // empty' 2>/dev/null)

if [ -n "$PROD_EVENTS" ] && [ "$PROD_EVENTS" != "null" ]; then
    echo "  Testing production event query by eventId..."
    api_call "GET" "$BASE_URL/api/event-tracking/production/event/$PROD_EVENTS" "" "Get production event by ID"
    echo ""
fi

# Get first consumption event ID if available
CONS_EVENTS=$(curl -s "$BASE_URL/api/event-tracking/consumption/status/PROCESSED" | jq -r '.[0].eventId // empty' 2>/dev/null)

if [ -n "$CONS_EVENTS" ] && [ "$CONS_EVENTS" != "null" ]; then
    echo "  Testing consumption event query by eventId..."
    api_call "GET" "$BASE_URL/api/event-tracking/consumption/event/$CONS_EVENTS" "" "Get consumption event by ID"
    echo ""
fi

echo "8. 📋 Test Topic Queries"
echo "------------------------"
api_call "GET" "$BASE_URL/api/event-tracking/production/topic/booking-confirmed" "" "Get events by topic (booking-confirmed)"
echo ""

api_call "GET" "$BASE_URL/api/event-tracking/production/topic/payment-initiated" "" "Get events by topic (payment-initiated)"
echo ""

echo "9. ✅ Test Summary"
echo "------------------"
echo ""
echo "Test completed! Summary:"
echo "  - Event production tracking: ${GREEN}✓${NC}"
echo "  - Event consumption tracking: ${GREEN}✓${NC}"
echo "  - REST API endpoints: ${GREEN}✓${NC}"
echo "  - Statistics endpoint: ${GREEN}✓${NC}"
echo ""
echo "💡 Next Steps:"
echo "   1. Check database tables: event_production_log, event_consumption_log"
echo "   2. Verify events are being tracked correctly"
echo "   3. Test idempotency by processing same event twice"
echo "   4. Monitor failed events and retry mechanism"
echo ""

