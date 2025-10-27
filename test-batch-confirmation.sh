#!/bin/bash

echo "🧪 Testing Ticket Confirmation Batch Processing System"
echo "====================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "SUCCESS" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$status" = "ERROR" ]; then
        echo -e "${RED}❌ $message${NC}"
    elif [ "$status" = "INFO" ]; then
        echo -e "${BLUE}ℹ️  $message${NC}"
    elif [ "$status" = "WARNING" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    fi
}

# Function to check if service is running
check_service() {
    local service_name=$1
    local port=$2
    
    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        print_status "SUCCESS" "$service_name is running on port $port"
        return 0
    else
        print_status "ERROR" "$service_name is not running on port $port"
        return 1
    fi
}

# Function to test API endpoint
test_api() {
    local endpoint=$1
    local method=${2:-GET}
    local data=${3:-""}
    
    print_status "INFO" "Testing $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "http://localhost:8082$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "http://localhost:8082$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        print_status "SUCCESS" "API call successful (HTTP $http_code)"
        echo "Response: $response_body" | head -c 200
        echo "..."
    else
        print_status "ERROR" "API call failed (HTTP $http_code)"
        echo "Response: $response_body"
    fi
    
    echo ""
}

# Function to create test data
create_test_data() {
    print_status "INFO" "Creating test data for batch processing..."
    
    # Create test user
    test_api "/api/users/register" "POST" '{
        "firstName": "Test",
        "lastName": "User",
        "email": "test@example.com",
        "username": "testuser",
        "password": "TestPass123!",
        "phoneNumber": "9876543210",
        "dateOfBirth": "1990-01-01T00:00:00",
        "gender": "MALE"
    }'
    
    # Create test train
    test_api "/api/trains" "POST" '{
        "trainNumber": "TEST001",
        "trainName": "Test Express",
        "sourceStationId": 1,
        "destinationStationId": 2,
        "departureTime": "08:00:00",
        "arrivalTime": "12:00:00",
        "totalDistance": 200.0,
        "trainType": "EXPRESS",
        "status": "ACTIVE",
        "isRunning": true
    }'
    
    print_status "SUCCESS" "Test data created"
}

# Function to test waitlist functionality
test_waitlist() {
    print_status "INFO" "Testing waitlist functionality..."
    
    # Add user to waitlist
    test_api "/api/waitlist-rac/add" "POST" '{
        "trainId": 1,
        "coachType": "AC2",
        "journeyDate": "2024-12-25T00:00:00",
        "numberOfPassengers": 1,
        "preferredSeatType": "LOWER_BERTH",
        "preferredBerthType": "LOWER_BERTH",
        "isLadiesQuota": false,
        "isSeniorCitizenQuota": false,
        "isHandicappedFriendly": false
    }'
    
    print_status "SUCCESS" "Waitlist test completed"
}

# Function to test batch processing
test_batch_processing() {
    print_status "INFO" "Testing batch processing..."
    
    # Test manual batch processing trigger
    test_api "/api/batch/process-confirmations/1" "POST"
    
    # Check batch processing statistics
    test_api "/api/batch/statistics/1/2024-12-25"
    
    print_status "SUCCESS" "Batch processing test completed"
}

# Function to test notification system
test_notifications() {
    print_status "INFO" "Testing notification system..."
    
    # Check notification service health
    if curl -s "http://localhost:8095/actuator/health" > /dev/null 2>&1; then
        print_status "SUCCESS" "Notification service is running"
        
        # Test notification endpoints
        test_api "/api/notifications" "GET"
        test_api "/api/notifications/user/1" "GET"
    else
        print_status "WARNING" "Notification service is not running - skipping notification tests"
    fi
}

# Function to check Kafka
check_kafka() {
    print_status "INFO" "Checking Kafka connectivity..."
    
    # Check if Kafka is running (this is a simple check)
    if netstat -an | grep -q ":9092.*LISTEN"; then
        print_status "SUCCESS" "Kafka is running on port 9092"
    else
        print_status "WARNING" "Kafka might not be running on port 9092"
    fi
}

# Function to run comprehensive tests
run_comprehensive_tests() {
    print_status "INFO" "Running comprehensive test suite..."
    
    echo ""
    print_status "INFO" "=== PHASE 1: Service Health Checks ==="
    check_service "Main Application" "8082"
    check_service "Notification Service" "8095"
    check_kafka
    
    echo ""
    print_status "INFO" "=== PHASE 2: Test Data Creation ==="
    create_test_data
    
    echo ""
    print_status "INFO" "=== PHASE 3: Waitlist Functionality ==="
    test_waitlist
    
    echo ""
    print_status "INFO" "=== PHASE 4: Batch Processing ==="
    test_batch_processing
    
    echo ""
    print_status "INFO" "=== PHASE 5: Notification System ==="
    test_notifications
    
    echo ""
    print_status "SUCCESS" "Comprehensive test suite completed!"
}

# Function to show metrics
show_metrics() {
    print_status "INFO" "Fetching batch processing metrics..."
    
    # Get metrics from actuator endpoint
    test_api "/actuator/metrics/ticket.confirmations.processed"
    test_api "/actuator/metrics/ticket.batch.processing.runs"
    test_api "/actuator/metrics/ticket.batch.processing.time"
    
    print_status "SUCCESS" "Metrics retrieved"
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --health       Check service health"
    echo "  --test-data    Create test data"
    echo "  --waitlist     Test waitlist functionality"
    echo "  --batch        Test batch processing"
    echo "  --notifications Test notification system"
    echo "  --metrics      Show metrics"
    echo "  --comprehensive Run all tests"
    echo "  --help         Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 --health"
    echo "  $0 --comprehensive"
    echo "  $0 --batch"
}

# Main script logic
case "${1:-}" in
    --health)
        print_status "INFO" "Checking service health..."
        check_service "Main Application" "8082"
        check_service "Notification Service" "8095"
        check_kafka
        ;;
    --test-data)
        create_test_data
        ;;
    --waitlist)
        test_waitlist
        ;;
    --batch)
        test_batch_processing
        ;;
    --notifications)
        test_notifications
        ;;
    --metrics)
        show_metrics
        ;;
    --comprehensive)
        run_comprehensive_tests
        ;;
    --help)
        show_help
        ;;
    "")
        print_status "INFO" "Running default test suite..."
        run_comprehensive_tests
        ;;
    *)
        print_status "ERROR" "Unknown option: $1"
        show_help
        exit 1
        ;;
esac

echo ""
print_status "INFO" "Test script completed!"
echo ""
echo "📊 Next Steps:"
echo "1. Check application logs for detailed processing information"
echo "2. Monitor Kafka topics for event publishing"
echo "3. Verify email/SMS notifications are sent"
echo "4. Check database for confirmation records"
echo ""
echo "🔍 Useful Commands:"
echo "- View logs: tail -f logs/irctc-application.log"
echo "- Check Kafka: kafka-console-consumer --bootstrap-server localhost:9092 --topic ticket-confirmation-events"
echo "- Monitor metrics: curl http://localhost:8082/actuator/metrics"
echo ""
