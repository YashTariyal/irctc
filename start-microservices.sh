#!/bin/bash

echo "🚀 Starting IRCTC Microservices Architecture"
echo "=============================================="

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo "📦 Starting $service_name on port $port..."
    cd $service_dir
    ./mvnw spring-boot:run > /tmp/$service_name.log 2>&1 &
    echo "✅ $service_name started (PID: $!)"
    cd ..
    sleep 5
}

# Start services in order
echo "1️⃣ Starting Eureka Server..."
start_service "eureka-server" "irctc-eureka-server" "8761"

echo "2️⃣ Starting API Gateway..."
start_service "api-gateway" "irctc-api-gateway" "8090"

echo "3️⃣ Starting User Service..."
start_service "user-service" "irctc-user-service" "8091"

echo "4️⃣ Starting Train Service..."
start_service "train-service" "irctc-train-service" "8092"

echo "5️⃣ Starting Booking Service..."
start_service "booking-service" "irctc-booking-service" "8093"

echo "6️⃣ Starting Payment Service..."
start_service "payment-service" "irctc-payment-service" "8094"

echo "7️⃣ Starting Notification Service..."
start_service "notification-service" "irctc-notification-service" "8095"

echo ""
echo "🎉 All microservices started successfully!"
echo ""
echo "📊 Service Status:"
echo "├── Eureka Server: http://localhost:8761"
echo "├── API Gateway: http://localhost:8090"
echo "├── User Service: http://localhost:8091"
echo "├── Train Service: http://localhost:8092"
echo "├── Booking Service: http://localhost:8093"
echo "├── Payment Service: http://localhost:8094"
echo "└── Notification Service: http://localhost:8095"
echo ""
echo "🔍 Check service logs:"
echo "├── Eureka: tail -f /tmp/eureka-server.log"
echo "├── API Gateway: tail -f /tmp/api-gateway.log"
echo "├── User Service: tail -f /tmp/user-service.log"
echo "├── Train Service: tail -f /tmp/train-service.log"
echo "├── Booking Service: tail -f /tmp/booking-service.log"
echo "├── Payment Service: tail -f /tmp/payment-service.log"
echo "└── Notification Service: tail -f /tmp/notification-service.log"
echo ""
echo "🛑 To stop all services: pkill -f spring-boot:run"
