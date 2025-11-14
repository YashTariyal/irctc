#!/bin/bash

# Analytics Dashboard Monitoring Script
# Continuously monitors analytics endpoints

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="${BASE_URL:-http://localhost:8083}"
INTERVAL="${INTERVAL:-30}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Analytics Dashboard Monitor${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Base URL: $BASE_URL"
echo "Update Interval: ${INTERVAL}s"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
echo ""

# Function to fetch and display analytics
display_analytics() {
    local endpoint=$1
    local name=$2
    
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}$name${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    RESPONSE=$(curl -s "$BASE_URL$endpoint" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ -n "$RESPONSE" ]; then
        echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    else
        echo -e "${RED}❌ Failed to fetch data${NC}"
    fi
    echo ""
}

# Main monitoring loop
while true; do
    clear
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Analytics Dashboard - $(date)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    # Overview
    display_analytics "/api/payments/analytics/overview" "📊 Overview"
    
    # Gateway Performance
    display_analytics "/api/payments/analytics/gateway-performance" "💳 Gateway Performance"
    
    # Payment Methods
    display_analytics "/api/payments/analytics/payment-methods" "💳 Payment Methods"
    
    # Gateway Statistics
    display_analytics "/api/payments/gateways/stats" "📈 Gateway Statistics"
    
    echo -e "${YELLOW}Next update in ${INTERVAL}s... (Press Ctrl+C to stop)${NC}"
    sleep $INTERVAL
done

