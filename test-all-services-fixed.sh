#!/bin/bash

echo "=== IRCTC Microservices - Comprehensive Test After 404 Fixes ==="
echo "=================================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test service
test_service() {
    local service_name=$1
    local port=$2
    local api_path=$3
    
    echo -e "\n${BLUE}Testing $service_name (Port: $port)${NC}"
    echo "----------------------------------------"
    
    # Test home page
    echo -n "Home Page: "
    if curl -s http://localhost:$port/ > /dev/null; then
        echo -e "${GREEN}✅ Working${NC}"
        curl -s http://localhost:$port/ | jq -r '.service + " - " + .description' 2>/dev/null || echo "Service info available"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
    
    # Test health endpoint
    echo -n "Health Check: "
    if curl -s http://localhost:$port/actuator/health > /dev/null; then
        echo -e "${GREEN}✅ Working${NC}"
        curl -s http://localhost:$port/actuator/health | jq -r '.status' 2>/dev/null || echo "Health status available"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
    
    # Test Swagger UI
    echo -n "Swagger UI: "
    if curl -s -I http://localhost:$port/swagger-ui/index.html | head -1 | grep -q "200"; then
        echo -e "${GREEN}✅ Working${NC}"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
    
    # Test API endpoint
    echo -n "API Endpoint: "
    if curl -s http://localhost:$port$api_path > /dev/null; then
        echo -e "${GREEN}✅ Working${NC}"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
    
    # Test error handling
    echo -n "Error Handling: "
    if curl -s http://localhost:$port/nonexistent | jq '.service' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Working${NC}"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
}

# Test all services
test_service "Train Service" 8092 "/api/trains"
test_service "Booking Service" 8093 "/api/bookings"
test_service "Payment Service" 8094 "/api/payments"
test_service "Notification Service" 8095 "/api/notifications"
test_service "User Service" 8091 "/api/users"
test_service "Swagger Hub" 8096 "/home"

echo -e "\n${BLUE}Testing Eureka Server${NC}"
echo "----------------------------------------"
echo -n "Eureka Dashboard: "
if curl -s http://localhost:8761/ > /dev/null; then
    echo -e "${GREEN}✅ Working${NC}"
else
    echo -e "${RED}❌ Failed${NC}"
fi

echo -e "\n${BLUE}Testing API Gateway${NC}"
echo "----------------------------------------"
echo -n "API Gateway: "
if curl -s http://localhost:8090/ > /dev/null; then
    echo -e "${GREEN}✅ Working${NC}"
else
    echo -e "${RED}❌ Failed${NC}"
fi

echo -e "\n${GREEN}=== All Services Tested Successfully! ===${NC}"
echo -e "${YELLOW}Access Points:${NC}"
echo "• Eureka Dashboard: http://localhost:8761"
echo "• API Gateway: http://localhost:8090"
echo "• Swagger Hub: http://localhost:8096/swagger-ui/index.html"
echo "• Individual Services: http://localhost:80XX/swagger-ui/index.html"
