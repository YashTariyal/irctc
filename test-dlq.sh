#!/bin/bash

# Test script for Dead Letter Queue (DLQ) functionality

set -e

BASE_URL="http://localhost:8095"
echo "🧪 TESTING DEAD LETTER QUEUE (DLQ) FUNCTIONALITY"
echo "================================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Step 1: Check DLQ Statistics
echo "📊 Step 1: Check DLQ Statistics..."
echo ""

DLQ_TOPICS=(
    "booking-created.DLT"
    "booking-confirmed.DLT"
    "booking-cancelled.DLT"
    "payment-initiated.DLT"
    "payment-completed.DLT"
    "ticket-confirmation-events.DLT"
    "user-events.DLT"
)

for topic in "${DLQ_TOPICS[@]}"; do
    echo "Checking: $topic"
    STATS=$(curl -s "${BASE_URL}/api/dlq/stats/${topic}" || echo "{}")
    COUNT=$(echo "${STATS}" | jq -r '.messageCount // 0' 2>/dev/null || echo "0")
    
    if [ "${COUNT}" -gt 0 ]; then
        echo -e "${YELLOW}⚠️  Found ${COUNT} messages in DLQ${NC}"
    else
        echo -e "${GREEN}✅ No messages in DLQ${NC}"
    fi
    echo ""
done

# Step 2: Get All DLQ Statistics
echo "📊 Step 2: Get All DLQ Statistics..."
ALL_STATS=$(curl -s "${BASE_URL}/api/dlq/stats")
echo "${ALL_STATS}" | jq '.' 2>/dev/null || echo "${ALL_STATS}"
echo ""

# Step 3: Inspect DLQ Messages (if any exist)
echo "🔍 Step 3: Inspect DLQ Messages..."
for topic in "${DLQ_TOPICS[@]}"; do
    STATS=$(curl -s "${BASE_URL}/api/dlq/stats/${topic}")
    COUNT=$(echo "${STATS}" | jq -r '.messageCount // 0' 2>/dev/null || echo "0")
    
    if [ "${COUNT}" -gt 0 ]; then
        echo "Inspecting messages in: $topic"
        MESSAGES=$(curl -s "${BASE_URL}/api/dlq/inspect/${topic}?maxMessages=5")
        echo "${MESSAGES}" | jq '.[0] | {offset, key, timestamp}' 2>/dev/null || echo "${MESSAGES}"
        echo ""
    fi
done

# Step 4: Test Reprocessing (if DLQ has messages)
echo "🔄 Step 4: Test DLQ Reprocessing..."
echo "Note: This will reprocess messages from DLQ back to main topic"
echo ""

# Example: Reprocess booking-created.DLT
if [ -n "${1}" ]; then
    DLT_TOPIC="${1}"
    MAIN_TOPIC=$(echo "${DLT_TOPIC}" | sed 's/\.DLT$//')
    
    echo "Reprocessing: ${DLT_TOPIC} -> ${MAIN_TOPIC}"
    REPROCESS_RESULT=$(curl -s -X POST "${BASE_URL}/api/dlq/reprocess" \
        -H "Content-Type: application/json" \
        -d "{
            \"dltTopic\": \"${DLT_TOPIC}\",
            \"mainTopic\": \"${MAIN_TOPIC}\",
            \"maxRecords\": 5
        }")
    
    echo "${REPROCESS_RESULT}" | jq '.' 2>/dev/null || echo "${REPROCESS_RESULT}"
else
    echo "Skipping reprocessing (provide DLQ topic as argument to test)"
    echo "Example: ./test-dlq.sh booking-created.DLT"
fi

echo ""
echo "✅ DLQ Testing Complete!"
echo ""
echo "📋 Available Endpoints:"
echo "   GET  /api/dlq/stats/{dltTopic} - Get DLQ statistics"
echo "   GET  /api/dlq/stats - Get all DLQ statistics"
echo "   GET  /api/dlq/inspect/{dltTopic}?maxMessages=10 - Inspect DLQ messages"
echo "   POST /api/dlq/reprocess - Reprocess DLQ messages"

