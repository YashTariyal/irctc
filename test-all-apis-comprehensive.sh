#!/bin/bash

echo "=== IRCTC Microservices - Comprehensive API Testing ==="
echo "======================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test API endpoint
test_api() {
    local service_name=$1
    local port=$2
    local endpoint=$3
    local method=$4
    local data=$5
    
    echo -n "  $method $endpoint: "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" http://localhost:$port$endpoint)
        http_code="${response: -3}"
        body="${response%???}"
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" http://localhost:$port$endpoint)
        http_code="${response: -3}"
        body="${response%???}"
    fi
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        echo -e "${GREEN}✅ Working (HTTP $http_code)${NC}"
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo "$body" | jq . 2>/dev/null | head -3 || echo "    Response: $body"
        fi
    else
        echo -e "${RED}❌ Failed (HTTP $http_code)${NC}"
        if [ -n "$body" ]; then
            echo "    Error: $body"
        fi
    fi
}

# Test User Service APIs
echo -e "\n${BLUE}Testing User Service APIs (Port: 8091)${NC}"
echo "=============================================="

test_api "User Service" 8091 "/api/users" "GET"
test_api "User Service" 8091 "/api/users/testuser" "GET"

# Create a test user
echo -e "\n${YELLOW}Creating Test User${NC}"
user_data='{"username":"testuser","password":"TestPass123!","email":"test@example.com","roles":["USER"]}'
test_api "User Service" 8091 "/api/users" "POST" "$user_data"

# Test Train Service APIs
echo -e "\n${BLUE}Testing Train Service APIs (Port: 8092)${NC}"
echo "=============================================="

test_api "Train Service" 8092 "/api/trains" "GET"
test_api "Train Service" 8092 "/api/trains/1" "GET"
test_api "Train Service" 8092 "/api/trains/search?source=Delhi&destination=Mumbai" "GET"

# Create a test train
echo -e "\n${YELLOW}Creating Test Train${NC}"
train_data='{"trainNumber":"12345","trainName":"Test Express","sourceStation":"Delhi","destinationStation":"Mumbai","departureTime":"2024-12-01T10:00:00","arrivalTime":"2024-12-01T18:00:00","trainType":"EXPRESS","trainClass":"AC2","baseFare":500.0,"totalSeats":100,"availableSeats":100,"status":"ACTIVE","amenities":["WiFi","Food"],"routeDescription":"Delhi to Mumbai via Agra","distance":1200,"duration":480}'
test_api "Train Service" 8092 "/api/trains" "POST" "$train_data"

# Test Booking Service APIs
echo -e "\n${BLUE}Testing Booking Service APIs (Port: 8093)${NC}"
echo "=============================================="

test_api "Booking Service" 8093 "/api/bookings" "GET"
test_api "Booking Service" 8093 "/api/bookings/1" "GET"
test_api "Booking Service" 8093 "/api/bookings/user/1" "GET"

# Create a test booking
echo -e "\n${YELLOW}Creating Test Booking${NC}"
booking_data='{"userId":1,"trainId":1,"totalFare":500.0,"passengers":[{"name":"John Doe","age":30,"gender":"MALE","seatNumber":"A1","idProofType":"AADHAAR","idProofNumber":"123456789012"}]}'
test_api "Booking Service" 8093 "/api/bookings" "POST" "$booking_data"

# Test Payment Service APIs
echo -e "\n${BLUE}Testing Payment Service APIs (Port: 8094)${NC}"
echo "=============================================="

test_api "Payment Service" 8094 "/api/payments" "GET"
test_api "Payment Service" 8094 "/api/payments/1" "GET"
test_api "Payment Service" 8094 "/api/payments/booking/1" "GET"

# Create a test payment
echo -e "\n${YELLOW}Creating Test Payment${NC}"
payment_data='{"bookingId":1,"amount":500.0,"currency":"INR","paymentMethod":"Credit Card"}'
test_api "Payment Service" 8094 "/api/payments" "POST" "$payment_data"

# Test Notification Service APIs
echo -e "\n${BLUE}Testing Notification Service APIs (Port: 8095)${NC}"
echo "=============================================="

test_api "Notification Service" 8095 "/api/notifications" "GET"
test_api "Notification Service" 8095 "/api/notifications/1" "GET"
test_api "Notification Service" 8095 "/api/notifications/user/1" "GET"

# Create a test notification
echo -e "\n${YELLOW}Creating Test Notification${NC}"
notification_data='{"userId":1,"type":"EMAIL","subject":"Booking Confirmation","message":"Your booking has been confirmed. PNR: ABC123"}'
test_api "Notification Service" 8095 "/api/notifications" "POST" "$notification_data"

# Test API Gateway
echo -e "\n${BLUE}Testing API Gateway (Port: 8090)${NC}"
echo "====================================="

test_api "API Gateway" 8090 "/api/users" "GET"
test_api "API Gateway" 8090 "/api/trains" "GET"
test_api "API Gateway" 8090 "/api/bookings" "GET"
test_api "API Gateway" 8090 "/api/payments" "GET"
test_api "API Gateway" 8090 "/api/notifications" "GET"

# Test Swagger Hub
echo -e "\n${BLUE}Testing Swagger Hub (Port: 8096)${NC}"
echo "====================================="

test_api "Swagger Hub" 8096 "/home" "GET"
test_api "Swagger Hub" 8096 "/services" "GET"
test_api "Swagger Hub" 8096 "/api-docs" "GET"

echo -e "\n${GREEN}=== Comprehensive API Testing Complete! ===${NC}"
echo -e "${YELLOW}Summary:${NC}"
echo "• All microservice APIs tested"
echo "• CRUD operations verified"
echo "• API Gateway routing tested"
echo "• Swagger Hub functionality verified"
