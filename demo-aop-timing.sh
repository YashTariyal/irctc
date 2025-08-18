#!/bin/bash

echo "🚀 IRCTC AOP Timing Demo"
echo "=========================="
echo ""

echo "📋 Building the project..."
./mvnw clean compile

echo ""
echo "🧪 Running AOP timing tests..."
echo "================================"
./mvnw test -Dtest=ExecutionTimeAspectTest -q

echo ""
echo "✅ Demo completed!"
echo ""
echo "📝 What you should see in the console:"
echo "   - 🌐 API Request logs"
echo "   - ✅ API Response logs with timing"
echo "   - 🚀 Starting execution logs"
echo "   - ✅ Completed execution logs"
echo "   - ⚠️  Slow operation warnings (if applicable)"
echo "   - ❌ Error logs with timing (if applicable)"
echo ""
echo "🔧 To see timing logs in real-time when running the application:"
echo "   ./mvnw spring-boot:run"
echo ""
echo "📊 The timing logs will appear in:"
echo "   - Console (as configured)"
echo "   - logs/irctc-api.log"
echo "   - logs/irctc-application.log"
echo "   - logs/irctc-json.log (structured format)"
