#!/bin/bash

# Comprehensive Test Suite for Revenue Analytics Dashboard Service
# Tests all analytics endpoints and service integration

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Service URLs
ANALYTICS_SERVICE="http://localhost:8096"
API_GATEWAY="http://localhost:8090"
EUREKA_SERVER="http://localhost:8761"
BOOKING_SERVICE="http://localhost:8093"
PAYMENT_SERVICE="http://localhost:8094"
TRAIN_SERVICE="http://localhost:8092"
USER_SERVICE="http://localhost:8091"

# Counters
TOTAL_TESTS=0
PASSED=0
FAILED=0

echo ""
echo "╔══════════════════════════════════════════════════════════════════════════╗"
echo "║  📊 Revenue Analytics Dashboard - Comprehensive Test Suite              ║"
echo "║  Testing: Analytics Service, API Gateway, Service Integration            ║"
echo "╚══════════════════════════════════════════════════════════════════════════╝"
echo ""

# Function to test service health
test_service_health() {
    local service_name=$1
    local service_url=$2
    local port=$3
    
    echo -n "  Testing $service_name (Port $port)... "
    if curl -s --max-time 5 "$service_url/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ UP${NC}"
        return 0
    else
        echo -e "${RED}❌ DOWN${NC}"
        return 1
    fi
}

# Function to test endpoint
test_endpoint() {
    local test_name=$1
    local endpoint=$2
    local expected_status=${3:-200}
    local method=${4:-GET}
    local data=${5:-""}
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "  Testing $test_name... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$endpoint" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ PASS${NC} (Status: $http_code)"
        PASSED=$((PASSED + 1))
        
        # Pretty print JSON if response is JSON
        if echo "$body" | grep -q "^{"; then
            echo "$body" | jq '.' 2>/dev/null || echo "$body" | head -c 200
            echo ""
        fi
        return 0
    else
        echo -e "${RED}❌ FAIL${NC} (Expected: $expected_status, Got: $http_code)"
        echo "    Response: $body" | head -c 200
        echo ""
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Function to check Eureka registration
check_eureka_registration() {
    echo -e "\n${BLUE}📋 Checking Eureka Service Registration...${NC}"
    
    if curl -s "$EUREKA_SERVER/eureka/apps" | grep -q "IRCTC-ANALYTICS-SERVICE" || \
       curl -s "$EUREKA_SERVER/eureka/apps" | grep -q "irctc-analytics-service"; then
        echo -e "  ${GREEN}✅ Analytics Service registered with Eureka${NC}"
        return 0
    else
        echo -e "  ${YELLOW}⚠️  Analytics Service not found in Eureka (may still be starting)${NC}"
        return 1
    fi
}

# Main test execution
main() {
    echo -e "${BLUE}🔍 Step 1: Checking Prerequisites${NC}"
    echo ""
    
    # Check required services
    local services_ok=0
    
    echo -e "${CYAN}Checking Required Services:${NC}"
    if test_service_health "Eureka Server" "$EUREKA_SERVER" "8761"; then
        services_ok=$((services_ok + 1))
    fi
    
    if test_service_health "Booking Service" "$BOOKING_SERVICE" "8093"; then
        services_ok=$((services_ok + 1))
    fi
    
    if test_service_health "Payment Service" "$PAYMENT_SERVICE" "8094"; then
        services_ok=$((services_ok + 1))
    fi
    
    if test_service_health "Train Service" "$TRAIN_SERVICE" "8092"; then
        services_ok=$((services_ok + 1))
    fi
    
    if test_service_health "User Service" "$USER_SERVICE" "8091"; then
        services_ok=$((services_ok + 1))
    fi
    
    if test_service_health "Analytics Service" "$ANALYTICS_SERVICE" "8096"; then
        services_ok=$((services_ok + 1))
    fi
    
    if test_service_health "API Gateway" "$API_GATEWAY" "8090"; then
        services_ok=$((services_ok + 1))
    fi
    
    echo ""
    echo -e "${BLUE}📊 Services Status: $services_ok/7 running${NC}"
    
    if [ $services_ok -lt 4 ]; then
        echo -e "${RED}❌ Insufficient services running. Need at least Booking, Payment, Train, and User services.${NC}"
        echo -e "${BLUE}💡 Start services using: ./start-microservices.sh${NC}"
        exit 1
    fi
    
    if [ $services_ok -lt 7 ]; then
        echo -e "${YELLOW}⚠️  Some services are not running. Some tests may fail or use fallbacks.${NC}"
    fi
    
    # Check Eureka registration
    check_eureka_registration
    
    echo ""
    echo -e "${BLUE}🧪 Step 2: Testing Analytics Service Endpoints (Direct)${NC}"
    echo ""
    
    # Test Analytics Service Health
    test_endpoint "Analytics Service Health" \
        "$ANALYTICS_SERVICE/actuator/health" \
        "200"
    
    # Test Swagger UI
    test_endpoint "Swagger UI" \
        "$ANALYTICS_SERVICE/swagger-ui.html" \
        "200"
    
    # Test API Docs
    test_endpoint "API Documentation" \
        "$ANALYTICS_SERVICE/api-docs" \
        "200"
    
    echo ""
    echo -e "${BLUE}📊 Step 3: Testing Revenue Analytics Endpoints${NC}"
    echo ""
    
    # Revenue Trends - Daily
    test_endpoint "Revenue Trends (Daily)" \
        "$ANALYTICS_SERVICE/api/analytics/revenue?period=daily" \
        "200"
    
    # Revenue Trends - Weekly
    test_endpoint "Revenue Trends (Weekly)" \
        "$ANALYTICS_SERVICE/api/analytics/revenue?period=weekly" \
        "200"
    
    # Revenue Trends - Monthly
    test_endpoint "Revenue Trends (Monthly)" \
        "$ANALYTICS_SERVICE/api/analytics/revenue?period=monthly" \
        "200"
    
    # Revenue Trends with Date Range
    test_endpoint "Revenue Trends (Date Range)" \
        "$ANALYTICS_SERVICE/api/analytics/revenue?period=daily&startDate=2025-01-01&endDate=2025-01-31" \
        "200"
    
    echo ""
    echo -e "${BLUE}📈 Step 4: Testing Booking Analytics Endpoints${NC}"
    echo ""
    
    # Booking Trends
    test_endpoint "Booking Trends" \
        "$ANALYTICS_SERVICE/api/analytics/bookings/trends" \
        "200"
    
    # Booking Trends with Date Range
    test_endpoint "Booking Trends (Date Range)" \
        "$ANALYTICS_SERVICE/api/analytics/bookings/trends?startDate=2025-01-01&endDate=2025-01-31" \
        "200"
    
    echo ""
    echo -e "${BLUE}🗺️  Step 5: Testing Route Performance Endpoints${NC}"
    echo ""
    
    # Route Performance
    test_endpoint "Route Performance" \
        "$ANALYTICS_SERVICE/api/analytics/routes/performance" \
        "200"
    
    # Route Performance with Date Range
    test_endpoint "Route Performance (Date Range)" \
        "$ANALYTICS_SERVICE/api/analytics/routes/performance?startDate=2025-01-01&endDate=2025-01-31" \
        "200"
    
    echo ""
    echo -e "${BLUE}👥 Step 6: Testing User Segmentation Endpoints${NC}"
    echo ""
    
    # User Segmentation
    test_endpoint "User Segmentation" \
        "$ANALYTICS_SERVICE/api/analytics/users/segmentation" \
        "200"
    
    echo ""
    echo -e "${BLUE}🔮 Step 7: Testing Forecasting Endpoints${NC}"
    echo ""
    
    # Forecast - Revenue
    test_endpoint "Forecast (Revenue)" \
        "$ANALYTICS_SERVICE/api/analytics/forecast?forecastType=revenue&days=30" \
        "200"
    
    # Forecast - Bookings
    test_endpoint "Forecast (Bookings)" \
        "$ANALYTICS_SERVICE/api/analytics/forecast?forecastType=bookings&days=30" \
        "200"
    
    echo ""
    echo -e "${BLUE}🌐 Step 8: Testing API Gateway Integration${NC}"
    echo ""
    
    if [ $services_ok -ge 7 ]; then
        # Test via API Gateway
        test_endpoint "API Gateway - Revenue Trends" \
            "$API_GATEWAY/api/analytics/revenue?period=daily" \
            "200"
        
        test_endpoint "API Gateway - Booking Trends" \
            "$API_GATEWAY/api/analytics/bookings/trends" \
            "200"
        
        test_endpoint "API Gateway - Route Performance" \
            "$API_GATEWAY/api/analytics/routes/performance" \
            "200"
        
        test_endpoint "API Gateway - User Segmentation" \
            "$API_GATEWAY/api/analytics/users/segmentation" \
            "200"
        
        test_endpoint "API Gateway - Forecast" \
            "$API_GATEWAY/api/analytics/forecast?forecastType=revenue&days=30" \
            "200"
    else
        echo -e "${YELLOW}⚠️  Skipping API Gateway tests (Gateway not running)${NC}"
    fi
    
    echo ""
    echo -e "${BLUE}🔍 Step 9: Testing Error Handling${NC}"
    echo ""
    
    # Test invalid period
    test_endpoint "Invalid Period Parameter" \
        "$ANALYTICS_SERVICE/api/analytics/revenue?period=invalid" \
        "200"  # Should default to daily
    
    # Test invalid forecast type
    test_endpoint "Invalid Forecast Type" \
        "$ANALYTICS_SERVICE/api/analytics/forecast?forecastType=invalid&days=30" \
        "200"  # Should default to revenue
    
    # Test invalid days
    test_endpoint "Invalid Days Parameter" \
        "$ANALYTICS_SERVICE/api/analytics/forecast?forecastType=revenue&days=999" \
        "200"  # Should default to 30
    
    echo ""
    echo "╔══════════════════════════════════════════════════════════════════════════╗"
    echo "║  📊 Test Results Summary                                                 ║"
    echo "╚══════════════════════════════════════════════════════════════════════════╝"
    echo ""
    echo -e "  Total Tests: ${CYAN}$TOTAL_TESTS${NC}"
    echo -e "  ${GREEN}✅ Passed: $PASSED${NC}"
    echo -e "  ${RED}❌ Failed: $FAILED${NC}"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed successfully!${NC}"
        echo ""
        echo -e "${BLUE}📋 Next Steps:${NC}"
        echo "  1. Access Swagger UI: http://localhost:8096/swagger-ui/index.html"
        echo "  2. Test via API Gateway: http://localhost:8090/api/analytics/*"
        echo "  3. Check Eureka Dashboard: http://localhost:8761"
        echo "  4. Review service logs for detailed information"
        return 0
    else
        echo -e "${YELLOW}⚠️  Some tests failed. Review the output above for details.${NC}"
        echo ""
        echo -e "${BLUE}💡 Troubleshooting:${NC}"
        echo "  1. Ensure all required services are running"
        echo "  2. Check service logs for errors"
        echo "  3. Verify Redis is running (for caching)"
        echo "  4. Check Eureka registration"
        return 1
    fi
}

# Run main function
main "$@"

