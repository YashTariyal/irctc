#!/bin/bash

# Script to restart all services and test AUD functionality

set -e

echo "🔄 RESTARTING ALL SERVICES AND TESTING AUD FUNCTIONALITY"
echo "========================================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Step 1: Stop all running services
echo "1️⃣  Stopping all running services..."
pkill -f "spring-boot:run" 2>/dev/null || echo "   No services to stop"
sleep 3
echo -e "${GREEN}✅ All services stopped${NC}"
echo ""

# Step 2: Start services in order
echo "2️⃣  Starting services..."
echo ""

# Start Eureka Server
echo "   📦 Starting Eureka Server..."
cd irctc-eureka-server
./mvnw spring-boot:run > /tmp/eureka-server.log 2>&1 &
EUREKA_PID=$!
cd ..
sleep 10
echo -e "${GREEN}   ✅ Eureka Server started (PID: ${EUREKA_PID})${NC}"
echo ""

# Start Booking Service (with audit infrastructure)
echo "   📦 Starting Booking Service (with AUD tables)..."
cd irctc-booking-service
./mvnw spring-boot:run > /tmp/booking-service.log 2>&1 &
BOOKING_PID=$!
cd ..
sleep 15
echo -e "${GREEN}   ✅ Booking Service started (PID: ${BOOKING_PID})${NC}"
echo ""

# Start Payment Service (with audit infrastructure)
echo "   📦 Starting Payment Service (with AUD tables)..."
cd irctc-payment-service
./mvnw spring-boot:run > /tmp/payment-service.log 2>&1 &
PAYMENT_PID=$!
cd ..
sleep 15
echo -e "${GREEN}   ✅ Payment Service started (PID: ${PAYMENT_PID})${NC}"
echo ""

# Start User Service (with audit infrastructure)
echo "   📦 Starting User Service (with AUD tables)..."
cd irctc-user-service
./mvnw spring-boot:run > /tmp/user-service.log 2>&1 &
USER_PID=$!
cd ..
sleep 15
echo -e "${GREEN}   ✅ User Service started (PID: ${USER_PID})${NC}"
echo ""

# Step 3: Wait for services to be ready
echo "3️⃣  Waiting for services to be ready..."
echo ""

wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "${url}/actuator/health" > /dev/null 2>&1; then
            local status=$(curl -s "${url}/actuator/health" | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
            if [ "${status}" = "UP" ]; then
                echo -e "${GREEN}   ✅ ${service_name} is UP${NC}"
                return 0
            fi
        fi
        echo "   ⏳ Waiting for ${service_name}... (attempt ${attempt}/${max_attempts})"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}   ❌ ${service_name} failed to start${NC}"
    return 1
}

wait_for_service "Eureka Server" "http://localhost:8761"
wait_for_service "Booking Service" "http://localhost:8093"
wait_for_service "Payment Service" "http://localhost:8094"
wait_for_service "User Service" "http://localhost:8091"

echo ""

# Step 4: Test Audit Functionality
echo "4️⃣  Testing AUD Functionality..."
echo ""

# Test Booking Service
echo "   🧪 Testing Booking Service Audit..."
if ./test-audit-diagnostic.sh > /tmp/audit-booking-test.log 2>&1; then
    echo -e "${GREEN}   ✅ Booking Service audit test completed${NC}"
    echo "   📄 See /tmp/audit-booking-test.log for details"
else
    echo -e "${YELLOW}   ⚠️  Booking Service audit test had issues${NC}"
    echo "   📄 See /tmp/audit-booking-test.log for details"
fi
echo ""

# Test Payment Service
echo "   🧪 Testing Payment Service Audit..."
if ./test-audit-payment.sh > /tmp/audit-payment-test.log 2>&1; then
    echo -e "${GREEN}   ✅ Payment Service audit test completed${NC}"
    echo "   📄 See /tmp/audit-payment-test.log for details"
else
    echo -e "${YELLOW}   ⚠️  Payment Service audit test had issues${NC}"
    echo "   📄 See /tmp/audit-payment-test.log for details"
fi
echo ""

# Test User Service
echo "   🧪 Testing User Service Audit..."
if ./test-audit-user.sh > /tmp/audit-user-test.log 2>&1; then
    echo -e "${GREEN}   ✅ User Service audit test completed${NC}"
    echo "   📄 See /tmp/audit-user-test.log for details"
else
    echo -e "${YELLOW}   ⚠️  User Service audit test had issues${NC}"
    echo "   📄 See /tmp/audit-user-test.log for details"
fi
echo ""

# Summary
echo "📊 SUMMARY"
echo "=========="
echo ""
echo "Service PIDs:"
echo "  - Eureka Server: ${EUREKA_PID}"
echo "  - Booking Service: ${BOOKING_PID}"
echo "  - Payment Service: ${PAYMENT_PID}"
echo "  - User Service: ${USER_PID}"
echo ""
echo "Test Logs:"
echo "  - Booking: /tmp/audit-booking-test.log"
echo "  - Payment: /tmp/audit-payment-test.log"
echo "  - User: /tmp/audit-user-test.log"
echo ""
echo "Service Logs:"
echo "  - Eureka: /tmp/eureka-server.log"
echo "  - Booking: /tmp/booking-service.log"
echo "  - Payment: /tmp/payment-service.log"
echo "  - User: /tmp/user-service.log"
echo ""
echo -e "${GREEN}🎉 Services restarted and audit tests completed!${NC}"
echo ""
echo "To stop all services: pkill -f spring-boot:run"

