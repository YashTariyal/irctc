#!/bin/bash

echo "🧪 Testing IRCTC Microservices with Swagger UI"
echo "=============================================="

# Function to test service
test_service() {
    local service_name=$1
    local port=$2
    local service_url=$3
    
    echo "🔍 Testing $service_name on port $port"
    
    # Test health endpoint
    echo "  Health Check:"
    curl -s http://localhost:$port/actuator/health | jq . || echo "  ❌ Health check failed"
    
    # Test API docs
    echo "  API Docs:"
    curl -s http://localhost:$port/api-docs | jq . || echo "  ❌ API docs not available"
    
    # Test Swagger UI
    echo "  Swagger UI:"
    curl -s http://localhost:$port/swagger-ui/index.html | head -5 || echo "  ❌ Swagger UI not available"
    
    echo "  ✅ $service_name testing completed"
    echo ""
}

# Test all services
echo "🚀 Testing All Microservices with Swagger UI"
echo ""

test_service "User Service" 8091 "http://localhost:8091"
test_service "Train Service" 8092 "http://localhost:8092"
test_service "Booking Service" 8093 "http://localhost:8093"
test_service "Payment Service" 8094 "http://localhost:8094"
test_service "Notification Service" 8095 "http://localhost:8095"

# Test Eureka
echo "🔍 Testing Eureka Server"
curl -s http://localhost:8761 | grep -i "eureka" || echo "❌ Eureka not accessible"

# Test API Gateway
echo "🔍 Testing API Gateway"
curl -s http://localhost:8090/actuator/health | jq . || echo "❌ API Gateway not accessible"

echo ""
echo "📚 Swagger UI Access URLs:"
echo "=========================="
echo "User Service:        http://localhost:8091/swagger-ui/index.html"
echo "Train Service:       http://localhost:8092/swagger-ui/index.html"
echo "Booking Service:     http://localhost:8093/swagger-ui/index.html"
echo "Payment Service:     http://localhost:8094/swagger-ui/index.html"
echo "Notification Service: http://localhost:8095/swagger-ui/index.html"
echo ""
echo "🌐 Eureka Dashboard: http://localhost:8761"
echo "🚪 API Gateway:     http://localhost:8090"
echo ""
echo "✅ Swagger UI testing completed!"
