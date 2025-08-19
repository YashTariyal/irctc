#!/bin/bash

echo "🧪 Testing IRCTC Dashboard Setup"
echo "================================"
echo ""

echo "📋 Checking project compilation..."
./mvnw clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Project compiles successfully"
else
    echo "❌ Compilation failed"
    exit 1
fi

echo ""
echo "🔍 Checking required files..."

# Check if template exists
if [ -f "src/main/resources/templates/dashboard.html" ]; then
    echo "✅ dashboard.html template found"
else
    echo "❌ dashboard.html template missing"
fi

# Check if CSS exists
if [ -f "src/main/resources/static/css/dashboard.css" ]; then
    echo "✅ dashboard.css found"
else
    echo "❌ dashboard.css missing"
fi

# Check if JS files exist
if [ -f "src/main/resources/static/js/dashboard.js" ]; then
    echo "✅ dashboard.js found"
else
    echo "❌ dashboard.js missing"
fi

if [ -f "src/main/resources/static/js/charts.js" ]; then
    echo "✅ charts.js found"
else
    echo "❌ charts.js missing"
fi

if [ -f "src/main/resources/static/js/websocket.js" ]; then
    echo "✅ websocket.js found"
else
    echo "❌ websocket.js missing"
fi

# Check if controllers exist
if [ -f "src/main/java/com/irctc_backend/irctc/controller/DashboardController.java" ]; then
    echo "✅ DashboardController.java found"
else
    echo "❌ DashboardController.java missing"
fi

if [ -f "src/main/java/com/irctc_backend/irctc/controller/DashboardApiController.java" ]; then
    echo "✅ DashboardApiController.java found"
else
    echo "❌ DashboardApiController.java missing"
fi

# Check if service exists
if [ -f "src/main/java/com/irctc_backend/irctc/service/DashboardService.java" ]; then
    echo "✅ DashboardService.java found"
else
    echo "❌ DashboardService.java missing"
fi

# Check if WebSocket config exists
if [ -f "src/main/java/com/irctc_backend/irctc/config/WebSocketConfig.java" ]; then
    echo "✅ WebSocketConfig.java found"
else
    echo "❌ WebSocketConfig.java missing"
fi

echo ""
echo "🔧 Dependencies check..."
if grep -q "spring-boot-starter-thymeleaf" pom.xml; then
    echo "✅ Thymeleaf dependency found"
else
    echo "❌ Thymeleaf dependency missing"
fi

if grep -q "spring-boot-starter-websocket" pom.xml; then
    echo "✅ WebSocket dependency found"
else
    echo "❌ WebSocket dependency missing"
fi

echo ""
echo "🎯 Next Steps:"
echo "1. Start the application: ./mvnw spring-boot:run"
echo "2. Open browser: http://localhost:8080/dashboard"
echo "3. Test API endpoints:"
echo "   - http://localhost:8080/dashboard/api/stats"
echo "   - http://localhost:8080/dashboard/api/activities"
echo "   - http://localhost:8080/dashboard/api/alerts"
echo ""
echo "✅ Dashboard setup verification completed!"
