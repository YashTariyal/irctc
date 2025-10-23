#!/bin/bash

echo "🧪 Testing All IRCTC Microservices APIs"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test API endpoint
test_api() {
    local service_name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=$5
    
    echo -e "${BLUE}Testing ${service_name}: ${method} ${url}${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X PUT -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X DELETE "$url")
    fi
    
    http_code="${response: -3}"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ ${service_name}: ${method} ${url} - Status: ${http_code}${NC}"
        if [ -f /tmp/response.json ] && [ -s /tmp/response.json ]; then
            echo "Response: $(cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json)"
        fi
    else
        echo -e "${RED}❌ ${service_name}: ${method} ${url} - Expected: ${expected_status}, Got: ${http_code}${NC}"
        if [ -f /tmp/response.json ] && [ -s /tmp/response.json ]; then
            echo "Error: $(cat /tmp/response.json)"
        fi
    fi
    echo ""
}

echo -e "${YELLOW}🚀 Starting Comprehensive API Testing${NC}"
echo ""

# Test User Service APIs
echo -e "${YELLOW}=== Testing User Service (Port 8091) ===${NC}"
test_api "User Service" "GET" "http://localhost:8091/actuator/health" "" "200"
test_api "User Service" "GET" "http://localhost:8091/api/users" "" "200"
test_api "User Service" "POST" "http://localhost:8091/api/users" '{"username":"testuser","password":"password123","email":"test@example.com","roles":["USER"]}' "200"
test_api "User Service" "GET" "http://localhost:8091/api/users/testuser" "" "200"
test_api "User Service" "POST" "http://localhost:8091/api/users/2fa/generate" '"testuser"' "200"
test_api "User Service" "POST" "http://localhost:8091/api/users/2fa/verify" '"123456"' "200"
test_api "User Service" "POST" "http://localhost:8091/api/users/password/validate" '"password123"' "200"

echo -e "${YELLOW}=== Testing Train Service (Port 8092) ===${NC}"
test_api "Train Service" "GET" "http://localhost:8092/actuator/health" "" "200"
test_api "Train Service" "GET" "http://localhost:8092/api/trains" "" "200"
test_api "Train Service" "GET" "http://localhost:8092/api/trains/search?source=Delhi&destination=Mumbai" "" "200"
test_api "Train Service" "GET" "http://localhost:8092/api/trains/available" "" "200"
test_api "Train Service" "GET" "http://localhost:8092/api/trains/type/EXPRESS" "" "200"
test_api "Train Service" "GET" "http://localhost:8092/api/trains/class/AC1" "" "200"
test_api "Train Service" "POST" "http://localhost:8092/api/trains" '{"trainNumber":"12345","trainName":"Test Express","sourceStation":"Delhi","destinationStation":"Mumbai","departureTime":"2024-01-01T10:00:00","arrivalTime":"2024-01-01T18:00:00","trainType":"EXPRESS","trainClass":"AC1","baseFare":500.0,"totalSeats":100,"availableSeats":100,"status":"ACTIVE","amenities":["WiFi","Food"],"routeDescription":"Delhi to Mumbai","distance":1000,"duration":480}' "200"

echo -e "${YELLOW}=== Testing Booking Service (Port 8093) ===${NC}"
test_api "Booking Service" "GET" "http://localhost:8093/actuator/health" "" "200"
test_api "Booking Service" "GET" "http://localhost:8093/api/bookings" "" "200"
test_api "Booking Service" "GET" "http://localhost:8093/api/bookings/user/1" "" "200"
test_api "Booking Service" "GET" "http://localhost:8093/api/bookings/status/CONFIRMED" "" "200"
test_api "Booking Service" "POST" "http://localhost:8093/api/bookings" '{"userId":1,"trainId":1,"totalFare":500.0,"passengers":[{"name":"John Doe","age":30,"gender":"Male","seatNumber":"A1","idProofType":"Aadhar","idProofNumber":"123456789012"}]}' "200"
test_api "Booking Service" "GET" "http://localhost:8093/api/bookings/date-range?startDate=2024-01-01&endDate=2024-01-31" "" "200"

echo -e "${YELLOW}=== Testing Payment Service (Port 8094) ===${NC}"
test_api "Payment Service" "GET" "http://localhost:8094/actuator/health" "" "200"
test_api "Payment Service" "GET" "http://localhost:8094/api/payments" "" "200"
test_api "Payment Service" "GET" "http://localhost:8094/api/payments/booking/1" "" "200"
test_api "Payment Service" "POST" "http://localhost:8094/api/payments" '{"bookingId":1,"amount":500.0,"currency":"INR","paymentMethod":"Credit Card","paymentStatus":"PENDING","paymentGateway":"Razorpay","gatewayTransactionId":"","paymentDate":"2024-01-01T10:00:00","contactEmail":"test@example.com","contactPhone":"9876543210"}' "200"
test_api "Payment Service" "GET" "http://localhost:8094/api/payments/payment-status/PENDING" "" "200"

echo -e "${YELLOW}=== Testing Notification Service (Port 8095) ===${NC}"
test_api "Notification Service" "GET" "http://localhost:8095/actuator/health" "" "200"
test_api "Notification Service" "GET" "http://localhost:8095/api/notifications" "" "200"
test_api "Notification Service" "GET" "http://localhost:8095/api/notifications/user/1" "" "200"
test_api "Notification Service" "GET" "http://localhost:8095/api/notifications/type/EMAIL" "" "200"
test_api "Notification Service" "POST" "http://localhost:8095/api/notifications" '{"userId":1,"notificationType":"EMAIL","notificationCategory":"BOOKING","title":"Booking Confirmation","message":"Your booking has been confirmed","recipientEmail":"test@example.com","status":"PENDING","scheduledTime":"2024-01-01T10:00:00","templateId":"BOOKING_CONFIRM","priority":"HIGH","isUrgent":false,"isRead":false,"isArchived":false}' "200"
test_api "Notification Service" "GET" "http://localhost:8095/api/notifications/status/PENDING" "" "200"

echo -e "${YELLOW}=== Testing Swagger Hub (Port 8096) ===${NC}"
test_api "Swagger Hub" "GET" "http://localhost:8096/actuator/health" "" "200"
test_api "Swagger Hub" "GET" "http://localhost:8096/services" "" "200"
test_api "Swagger Hub" "GET" "http://localhost:8096/" "" "200"

echo -e "${YELLOW}=== Testing API Gateway (Port 8090) ===${NC}"
test_api "API Gateway" "GET" "http://localhost:8090/actuator/health" "" "200"
test_api "API Gateway" "GET" "http://localhost:8090/api/users" "" "200"
test_api "API Gateway" "GET" "http://localhost:8090/api/trains" "" "200"
test_api "API Gateway" "GET" "http://localhost:8090/api/bookings" "" "200"
test_api "API Gateway" "GET" "http://localhost:8090/api/payments" "" "200"
test_api "API Gateway" "GET" "http://localhost:8090/api/notifications" "" "200"

echo -e "${YELLOW}=== Testing Eureka Server (Port 8761) ===${NC}"
test_api "Eureka Server" "GET" "http://localhost:8761" "" "200"

echo -e "${YELLOW}=== Testing Swagger UI Access ===${NC}"
echo -e "${BLUE}Testing Swagger UI endpoints:${NC}"
test_api "User Service Swagger" "GET" "http://localhost:8091/swagger-ui/index.html" "" "200"
test_api "Train Service Swagger" "GET" "http://localhost:8092/swagger-ui/index.html" "" "200"
test_api "Booking Service Swagger" "GET" "http://localhost:8093/swagger-ui/index.html" "" "200"
test_api "Payment Service Swagger" "GET" "http://localhost:8094/swagger-ui/index.html" "" "200"
test_api "Notification Service Swagger" "GET" "http://localhost:8095/swagger-ui/index.html" "" "200"
test_api "Swagger Hub Swagger" "GET" "http://localhost:8096/swagger-ui/index.html" "" "200"

echo -e "${YELLOW}=== Testing API Docs (JSON) ===${NC}"
echo -e "${BLUE}Testing API Docs endpoints:${NC}"
test_api "User Service API Docs" "GET" "http://localhost:8091/api-docs" "" "200"
test_api "Train Service API Docs" "GET" "http://localhost:8092/api-docs" "" "200"
test_api "Booking Service API Docs" "GET" "http://localhost:8093/api-docs" "" "200"
test_api "Payment Service API Docs" "GET" "http://localhost:8094/api-docs" "" "200"
test_api "Notification Service API Docs" "GET" "http://localhost:8095/api-docs" "" "200"
test_api "Swagger Hub API Docs" "GET" "http://localhost:8096/api-docs" "" "200"

echo -e "${GREEN}🎉 All API Testing Completed!${NC}"
echo ""
echo -e "${YELLOW}📊 Summary:${NC}"
echo "✅ User Service: http://localhost:8091"
echo "✅ Train Service: http://localhost:8092"
echo "✅ Booking Service: http://localhost:8093"
echo "✅ Payment Service: http://localhost:8094"
echo "✅ Notification Service: http://localhost:8095"
echo "✅ Swagger Hub: http://localhost:8096"
echo "✅ API Gateway: http://localhost:8090"
echo "✅ Eureka Server: http://localhost:8761"
echo ""
echo -e "${YELLOW}📚 Swagger UI Access:${NC}"
echo "🔗 User Service: http://localhost:8091/swagger-ui/index.html"
echo "🔗 Train Service: http://localhost:8092/swagger-ui/index.html"
echo "🔗 Booking Service: http://localhost:8093/swagger-ui/index.html"
echo "🔗 Payment Service: http://localhost:8094/swagger-ui/index.html"
echo "🔗 Notification Service: http://localhost:8095/swagger-ui/index.html"
echo "🔗 Swagger Hub: http://localhost:8096/swagger-ui/index.html"
echo ""
echo -e "${YELLOW}🌐 Eureka Dashboard: http://localhost:8761${NC}"
echo -e "${YELLOW}🚪 API Gateway: http://localhost:8090${NC}"
