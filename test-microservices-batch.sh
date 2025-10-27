#!/bin/bash

# 🧪 Test Script for IRCTC Microservices Batch Processing
# Tests the ticket confirmation batch processing system across microservices

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BOOKING_SERVICE_URL="http://localhost:8093"
NOTIFICATION_SERVICE_URL="http://localhost:8095"
KAFKA_TOPIC="ticket-confirmation-events"

echo -e "${BLUE}🧪 Testing IRCTC Microservices Batch Processing System${NC}"
echo "====================================================="

# Function to check if service is running
check_service() {
    local service_name=$1
    local url=$2
    local port=$3
    
    echo -e "${BLUE}ℹ️  Checking $service_name...${NC}"
    
    if curl -s --connect-timeout 5 "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $service_name is running on port $port${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not running on port $port${NC}"
        return 1
    fi
}

# Function to test booking service batch processing
test_booking_service() {
    echo -e "\n${BLUE}🎫 Testing Booking Service Batch Processing${NC}"
    echo "--------------------------------------------"
    
    # Test health endpoint
    echo -e "${BLUE}ℹ️  Testing health endpoint...${NC}"
    if curl -s "$BOOKING_SERVICE_URL/api/batch/health" | grep -q "healthy"; then
        echo -e "${GREEN}✅ Booking service health check passed${NC}"
    else
        echo -e "${RED}❌ Booking service health check failed${NC}"
        return 1
    fi
    
    # Test service info
    echo -e "${BLUE}ℹ️  Testing service info endpoint...${NC}"
    if curl -s "$BOOKING_SERVICE_URL/api/batch/info" | grep -q "IRCTC Booking Service"; then
        echo -e "${GREEN}✅ Service info endpoint working${NC}"
    else
        echo -e "${RED}❌ Service info endpoint failed${NC}"
        return 1
    fi
    
    # Test statistics endpoint
    echo -e "${BLUE}ℹ️  Testing statistics endpoint...${NC}"
    if curl -s "$BOOKING_SERVICE_URL/api/batch/statistics" | grep -q "success"; then
        echo -e "${GREEN}✅ Statistics endpoint working${NC}"
    else
        echo -e "${RED}❌ Statistics endpoint failed${NC}"
        return 1
    fi
    
    return 0
}

# Function to test manual batch trigger
test_manual_trigger() {
    echo -e "\n${BLUE}🔧 Testing Manual Batch Trigger${NC}"
    echo "-----------------------------------"
    
    echo -e "${BLUE}ℹ️  Triggering manual batch processing...${NC}"
    response=$(curl -s -X POST "$BOOKING_SERVICE_URL/api/batch/trigger-confirmations")
    
    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ Manual batch trigger successful${NC}"
        echo -e "${BLUE}📊 Response: $response${NC}"
    else
        echo -e "${RED}❌ Manual batch trigger failed${NC}"
        echo -e "${RED}📊 Response: $response${NC}"
        return 1
    fi
    
    return 0
}

# Function to test notification service
test_notification_service() {
    echo -e "\n${BLUE}📧 Testing Notification Service${NC}"
    echo "----------------------------------"
    
    # Test health endpoint
    echo -e "${BLUE}ℹ️  Testing notification service health...${NC}"
    if curl -s "$NOTIFICATION_SERVICE_URL/actuator/health" | grep -q "UP"; then
        echo -e "${GREEN}✅ Notification service is healthy${NC}"
    else
        echo -e "${RED}❌ Notification service health check failed${NC}"
        return 1
    fi
    
    return 0
}

# Function to test Kafka connectivity
test_kafka() {
    echo -e "\n${BLUE}📡 Testing Kafka Connectivity${NC}"
    echo "-------------------------------"
    
    echo -e "${BLUE}ℹ️  Checking Kafka topic: $KAFKA_TOPIC${NC}"
    
    # Check if Kafka is running
    if netstat -an | grep -q ":9092.*LISTEN"; then
        echo -e "${GREEN}✅ Kafka is running on port 9092${NC}"
    else
        echo -e "${YELLOW}⚠️  Kafka might not be running on port 9092${NC}"
    fi
    
    return 0
}

# Function to run comprehensive test
run_comprehensive_test() {
    echo -e "\n${BLUE}🚀 Running Comprehensive Test${NC}"
    echo "================================"
    
    local test_passed=0
    
    # Test booking service
    if test_booking_service; then
        echo -e "${GREEN}✅ Booking service tests passed${NC}"
    else
        echo -e "${RED}❌ Booking service tests failed${NC}"
        test_passed=1
    fi
    
    # Test manual trigger
    if test_manual_trigger; then
        echo -e "${GREEN}✅ Manual trigger test passed${NC}"
    else
        echo -e "${RED}❌ Manual trigger test failed${NC}"
        test_passed=1
    fi
    
    # Test notification service
    if test_notification_service; then
        echo -e "${GREEN}✅ Notification service tests passed${NC}"
    else
        echo -e "${RED}❌ Notification service tests failed${NC}"
        test_passed=1
    fi
    
    # Test Kafka
    if test_kafka; then
        echo -e "${GREEN}✅ Kafka connectivity test passed${NC}"
    else
        echo -e "${RED}❌ Kafka connectivity test failed${NC}"
        test_passed=1
    fi
    
    return $test_passed
}

# Main execution
main() {
    echo -e "${BLUE}ℹ️  Starting microservices batch processing tests...${NC}"
    
    # Check if services are running
    local services_running=0
    
    if check_service "Booking Service" "$BOOKING_SERVICE_URL" "8093"; then
        services_running=$((services_running + 1))
    fi
    
    if check_service "Notification Service" "$NOTIFICATION_SERVICE_URL" "8095"; then
        services_running=$((services_running + 1))
    fi
    
    echo -e "\n${BLUE}📊 Services Status: $services_running/2 running${NC}"
    
    if [ $services_running -eq 0 ]; then
        echo -e "${RED}❌ No services are running. Please start the microservices first.${NC}"
        echo -e "${BLUE}💡 Use: ./start-microservices.sh${NC}"
        exit 1
    elif [ $services_running -eq 1 ]; then
        echo -e "${YELLOW}⚠️  Only 1 service is running. Some tests may fail.${NC}"
    fi
    
    # Run tests based on arguments
    case "${1:-comprehensive}" in
        "health")
            test_booking_service
            test_notification_service
            ;;
        "booking")
            test_booking_service
            ;;
        "trigger")
            test_manual_trigger
            ;;
        "notification")
            test_notification_service
            ;;
        "kafka")
            test_kafka
            ;;
        "comprehensive")
            run_comprehensive_test
            ;;
        *)
            echo -e "${RED}❌ Unknown test type: $1${NC}"
            echo -e "${BLUE}💡 Available options: health, booking, trigger, notification, kafka, comprehensive${NC}"
            exit 1
            ;;
    esac
    
    local exit_code=$?
    
    echo -e "\n${BLUE}ℹ️  Test script completed!${NC}"
    
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed successfully!${NC}"
    else
        echo -e "${RED}❌ Some tests failed. Check the output above for details.${NC}"
    fi
    
    echo -e "\n${BLUE}📊 Next Steps:${NC}"
    echo "1. Check application logs for detailed processing information"
    echo "2. Monitor Kafka topics for event publishing"
    echo "3. Verify email/SMS notifications are sent"
    echo "4. Check database for confirmation records"
    
    echo -e "\n${BLUE}🔍 Useful Commands:${NC}"
    echo "- View booking service logs: tail -f irctc-booking-service/logs/booking-service.log"
    echo "- View notification service logs: tail -f irctc-notification-service/logs/notification-service.log"
    echo "- Check Kafka: kafka-console-consumer --bootstrap-server localhost:9092 --topic $KAFKA_TOPIC"
    echo "- Monitor metrics: curl $BOOKING_SERVICE_URL/api/batch/statistics"
    
    exit $exit_code
}

# Run main function with all arguments
main "$@"
