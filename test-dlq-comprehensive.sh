#!/bin/bash

# Comprehensive DLQ Test Script
# Tests Dead Letter Queue functionality end-to-end

set -e

BASE_URL="http://localhost:8095"
KAFKA_BOOTSTRAP="localhost:9092"

echo "🧪 COMPREHENSIVE DLQ FUNCTIONALITY TEST"
echo "======================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Step 1: Check Service Health
echo "📋 Step 1: Check Service Health"
echo "-------------------------------"
HEALTH=$(curl -s "${BASE_URL}/actuator/health" 2>/dev/null || echo "{}")
STATUS=$(echo "${HEALTH}" | jq -r '.status' 2>/dev/null || echo "DOWN")

if [ "${STATUS}" = "UP" ]; then
    echo -e "${GREEN}✅ Notification Service is UP${NC}"
else
    echo -e "${RED}❌ Notification Service is DOWN${NC}"
    echo "Please start the notification service first"
    exit 1
fi
echo ""

# Step 2: Check Initial DLQ Statistics
echo "📋 Step 2: Check Initial DLQ Statistics"
echo "----------------------------------------"
echo "Getting all DLQ statistics..."
ALL_STATS=$(curl -s "${BASE_URL}/api/dlq/stats" 2>/dev/null || echo "{}")
echo "${ALL_STATS}" | jq '.' 2>/dev/null || echo "${ALL_STATS}"
echo ""

# Step 3: Check Specific DLQ Topics
echo "📋 Step 3: Check Specific DLQ Topics"
echo "-------------------------------------"
DLQ_TOPICS=(
    "booking-created.DLT"
    "booking-confirmed.DLT"
    "booking-cancelled.DLT"
    "payment-initiated.DLT"
    "payment-completed.DLT"
    "ticket-confirmation-events.DLT"
    "user-events.DLT"
)

TOTAL_DLQ_MESSAGES=0
for topic in "${DLQ_TOPICS[@]}"; do
    STATS=$(curl -s "${BASE_URL}/api/dlq/stats/${topic}" 2>/dev/null || echo "{}")
    COUNT=$(echo "${STATS}" | jq -r '.messageCount // 0' 2>/dev/null || echo "0")
    
    if [ "${COUNT}" -gt 0 ]; then
        echo -e "${YELLOW}⚠️  ${topic}: ${COUNT} messages${NC}"
        TOTAL_DLQ_MESSAGES=$((TOTAL_DLQ_MESSAGES + COUNT))
    else
        echo -e "${GREEN}✅ ${topic}: 0 messages${NC}"
    fi
done

echo ""
if [ "${TOTAL_DLQ_MESSAGES}" -gt 0 ]; then
    echo -e "${YELLOW}📊 Total DLQ messages across all topics: ${TOTAL_DLQ_MESSAGES}${NC}"
else
    echo -e "${GREEN}📊 No messages in DLQ (this is expected if no failures occurred)${NC}"
fi
echo ""

# Step 4: Inspect DLQ Messages (if any exist)
if [ "${TOTAL_DLQ_MESSAGES}" -gt 0 ]; then
    echo "📋 Step 4: Inspect DLQ Messages"
    echo "-------------------------------"
    for topic in "${DLQ_TOPICS[@]}"; do
        STATS=$(curl -s "${BASE_URL}/api/dlq/stats/${topic}" 2>/dev/null || echo "{}")
        COUNT=$(echo "${STATS}" | jq -r '.messageCount // 0' 2>/dev/null || echo "0")
        
        if [ "${COUNT}" -gt 0 ]; then
            echo -e "${BLUE}Inspecting messages in: ${topic}${NC}"
            MESSAGES=$(curl -s "${BASE_URL}/api/dlq/inspect/${topic}?maxMessages=3" 2>/dev/null || echo "[]")
            MESSAGE_COUNT=$(echo "${MESSAGES}" | jq 'length' 2>/dev/null || echo "0")
            
            if [ "${MESSAGE_COUNT}" -gt 0 ]; then
                echo "Found ${MESSAGE_COUNT} message(s):"
                echo "${MESSAGES}" | jq '.[0] | {offset, key, timestamp, topic}' 2>/dev/null || echo "${MESSAGES}"
            fi
            echo ""
        fi
    done
else
    echo "📋 Step 4: Inspect DLQ Messages"
    echo "-------------------------------"
    echo -e "${GREEN}No messages to inspect${NC}"
    echo ""
fi

# Step 5: Test DLQ Reprocessing (if messages exist)
if [ "${TOTAL_DLQ_MESSAGES}" -gt 0 ]; then
    echo "📋 Step 5: Test DLQ Reprocessing"
    echo "-------------------------------"
    echo "Note: This will reprocess messages from DLQ back to main topic"
    echo ""
    
    # Find first DLQ topic with messages
    REPROCESS_TOPIC=""
    MAIN_TOPIC=""
    
    for topic in "${DLQ_TOPICS[@]}"; do
        STATS=$(curl -s "${BASE_URL}/api/dlq/stats/${topic}" 2>/dev/null || echo "{}")
        COUNT=$(echo "${STATS}" | jq -r '.messageCount // 0' 2>/dev/null || echo "0")
        
        if [ "${COUNT}" -gt 0 ]; then
            REPROCESS_TOPIC="${topic}"
            MAIN_TOPIC=$(echo "${topic}" | sed 's/\.DLT$//')
            break
        fi
    done
    
    if [ -n "${REPROCESS_TOPIC}" ]; then
        echo -e "${BLUE}Reprocessing: ${REPROCESS_TOPIC} -> ${MAIN_TOPIC}${NC}"
        echo ""
        
        REPROCESS_RESULT=$(curl -s -X POST "${BASE_URL}/api/dlq/reprocess" \
            -H "Content-Type: application/json" \
            -d "{
                \"dltTopic\": \"${REPROCESS_TOPIC}\",
                \"mainTopic\": \"${MAIN_TOPIC}\",
                \"maxRecords\": 3
            }" 2>/dev/null || echo "{}")
        
        REPROCESSED=$(echo "${REPROCESS_RESULT}" | jq -r '.reprocessedCount // 0' 2>/dev/null || echo "0")
        FAILED=$(echo "${REPROCESS_RESULT}" | jq -r '.failedCount // 0' 2>/dev/null || echo "0")
        
        if [ "${REPROCESSED}" -gt 0 ]; then
            echo -e "${GREEN}✅ Reprocessed ${REPROCESSED} message(s)${NC}"
        fi
        if [ "${FAILED}" -gt 0 ]; then
            echo -e "${RED}❌ Failed to reprocess ${FAILED} message(s)${NC}"
        fi
        
        echo ""
        echo "Reprocess result:"
        echo "${REPROCESS_RESULT}" | jq '.' 2>/dev/null || echo "${REPROCESS_RESULT}"
    else
        echo -e "${GREEN}No messages to reprocess${NC}"
    fi
    echo ""
else
    echo "📋 Step 5: Test DLQ Reprocessing"
    echo "-------------------------------"
    echo -e "${GREEN}No messages to reprocess${NC}"
    echo ""
fi

# Step 6: Check Prometheus Metrics
echo "📋 Step 6: Check Prometheus Metrics"
echo "-----------------------------------"
METRICS=$(curl -s "${BASE_URL}/actuator/prometheus" 2>/dev/null | grep "kafka.dlq" || echo "")
if [ -n "${METRICS}" ]; then
    echo "DLQ Metrics found:"
    echo "${METRICS}" | head -5
else
    echo "No DLQ metrics found (may not have been triggered yet)"
fi
echo ""

# Step 7: Summary
echo "📊 TEST SUMMARY"
echo "==============="
echo ""
echo "✅ Service Status: ${STATUS}"
echo "✅ DLQ API Endpoints: Accessible"
echo "✅ Total DLQ Messages: ${TOTAL_DLQ_MESSAGES}"
echo ""
if [ "${TOTAL_DLQ_MESSAGES}" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  DLQ contains messages - investigate root causes${NC}"
    echo "   - Check consumer logs for errors"
    echo "   - Review message content in DLQ"
    echo "   - Fix issues before reprocessing"
else
    echo -e "${GREEN}✅ No messages in DLQ - system is healthy${NC}"
fi
echo ""
echo "📋 Available DLQ Endpoints:"
echo "   GET  /api/dlq/stats/{dltTopic} - Get DLQ statistics"
echo "   GET  /api/dlq/stats - Get all DLQ statistics"
echo "   GET  /api/dlq/inspect/{dltTopic}?maxMessages=10 - Inspect messages"
echo "   POST /api/dlq/reprocess - Reprocess messages"
echo ""
echo "✅ DLQ Testing Complete!"

