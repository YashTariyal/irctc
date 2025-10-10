#!/bin/bash

echo "🧪 Testing IRCTC Application Logging Configuration"
echo "=================================================="

# Create logs directory if it doesn't exist
mkdir -p logs

# Set proper permissions
chmod 755 logs

echo "📁 Logs directory created with proper permissions"
echo "📋 Log files that should be generated:"
echo "   - logs/irctc-application.log (main application logs)"
echo "   - logs/irctc-error.log (error logs)"
echo "   - logs/irctc-api.log (API request logs)"
echo "   - logs/irctc-database.log (database/SQL logs)"
echo "   - logs/irctc-kafka.log (Kafka logs)"
echo "   - logs/irctc-security.log (security logs)"
echo "   - logs/irctc-json.log (structured JSON logs)"
echo ""

echo "🚀 Starting application to test logging..."
echo "   (This will run for 10 seconds to generate logs)"
echo ""

# Start the application in background
mvn spring-boot:run > /dev/null 2>&1 &
APP_PID=$!

# Wait for application to start
sleep 15

echo "📊 Checking for generated log files..."
echo ""

# Check which log files were created
for log_file in "irctc-application.log" "irctc-error.log" "irctc-api.log" "irctc-database.log" "irctc-kafka.log" "irctc-security.log" "irctc-json.log"; do
    if [ -f "logs/$log_file" ]; then
        echo "✅ $log_file - EXISTS ($(wc -l < logs/$log_file) lines)"
    else
        echo "❌ $log_file - NOT FOUND"
    fi
done

echo ""
echo "📋 Sample content from irctc-application.log:"
if [ -f "logs/irctc-application.log" ]; then
    echo "   (Last 5 lines)"
    tail -5 logs/irctc-application.log | sed 's/^/   /'
else
    echo "   No application log file found"
fi

# Stop the application
echo ""
echo "🛑 Stopping application..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

echo ""
echo "✅ Logging test completed!"
echo "📁 Check the logs/ directory for generated log files"
