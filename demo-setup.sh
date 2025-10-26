#!/bin/bash

echo "🚀 IRCTC Demo Setup Script"
echo "=========================="
echo ""

echo "📋 This script will set up a complete IRCTC demo environment"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo "⚠️  Port $port is already in use"
        return 1
    else
        echo "✅ Port $port is available"
        return 0
    fi
}

echo "🔍 Checking port availability..."
check_port 8080  # Frontend
check_port 8082  # Backend
check_port 8761  # Eureka
check_port 8090  # API Gateway
check_port 5432  # PostgreSQL
check_port 6379  # Redis
check_port 9092  # Kafka

echo ""
echo "🐳 Starting Docker containers..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to start..."
sleep 30

echo ""
echo "🎉 Demo Environment Ready!"
echo ""
echo "🌐 Access Points:"
echo "├── 🎨 Frontend Dashboard: http://localhost:3000"
echo "├── 🔧 Backend APIs: http://localhost:8082"
echo "├── 📚 Swagger UI: http://localhost:8082/swagger-ui.html"
echo "├── 📊 Eureka Dashboard: http://localhost:8761"
echo "├── 🌐 API Gateway: http://localhost:8090"
echo "├── 🗄️  Kafka UI: http://localhost:8080"
echo "└── 📈 Performance Dashboard: http://localhost:8082/dashboard"
echo ""
echo "🔑 Demo Credentials:"
echo "├── Username: admin"
echo "├── Password: admin123"
echo "└── Email: admin@irctc.com"
echo ""
echo "📱 Demo Features:"
echo "├── ✅ User Registration & Login"
echo "├── ✅ Train Search & Booking"
echo "├── ✅ Seat Selection"
echo "├── ✅ Payment Processing"
echo "├── ✅ Real-time Notifications"
echo "├── ✅ Loyalty Points System"
echo "├── ✅ Travel Insurance"
echo "├── ✅ Meal Booking"
echo "└── ✅ Multi-city Trip Planning"
echo ""
echo "🛑 To stop the demo: docker-compose down"
echo "📊 To view logs: docker-compose logs -f"
echo ""
echo "🎯 Ready for demo! Open http://localhost:3000 to start"
