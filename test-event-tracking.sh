#!/bin/bash

# Test Event Tracking Implementation
# This script tests the event tracking functionality

echo "🧪 Testing Event Tracking Implementation"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8093"

echo "1. 📊 Getting Event Tracking Statistics"
echo "----------------------------------------"
curl -s "$BASE_URL/api/event-tracking/stats" | jq '.'
echo ""

echo "2. 📤 Getting Production Logs (PENDING)"
echo "----------------------------------------"
curl -s "$BASE_URL/api/event-tracking/production/status/PENDING" | jq '.[0:3]'
echo ""

echo "3. 📥 Getting Consumption Logs (RECEIVED)"
echo "------------------------------------------"
curl -s "$BASE_URL/api/event-tracking/consumption/status/RECEIVED" | jq '.[0:3]'
echo ""

echo "4. ❌ Getting Failed Production Events"
echo "---------------------------------------"
curl -s "$BASE_URL/api/event-tracking/production/failed" | jq '.[0:3]'
echo ""

echo "5. ❌ Getting Failed Consumption Events"
echo "----------------------------------------"
curl -s "$BASE_URL/api/event-tracking/consumption/failed" | jq '.[0:3]'
echo ""

echo "✅ Event Tracking Test Complete!"
echo ""
echo "💡 To test event production:"
echo "   - Create a booking (this will publish events)"
echo "   - Check production logs: curl $BASE_URL/api/event-tracking/production/status/PUBLISHED"
echo ""
echo "💡 To test event consumption:"
echo "   - Events will be automatically logged when consumed"
echo "   - Check consumption logs: curl $BASE_URL/api/event-tracking/consumption/status/PROCESSED"

